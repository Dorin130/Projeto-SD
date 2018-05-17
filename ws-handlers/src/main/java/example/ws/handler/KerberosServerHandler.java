package example.ws.handler;

import pt.ulisboa.tecnico.sdis.kerby.*;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.security.Key;
import java.util.Set;
import javax.xml.soap.*;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ConcurrentHashMap;


public class KerberosServerHandler implements SOAPHandler<SOAPMessageContext> {
    private static final String SESSION_KEY = "sessionKey";
    private static final String REQUEST_TIME = "requestTime";
    private static final String CLIENT_NAME = "clientName";

    Properties properties;
    Key serverKey;
    long timeLeeway = 1000;
    static Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    public KerberosServerHandler() {
        super();
        this.properties = new Properties();

        // load configuration properties
        try {
            InputStream inputStream = KerberosClientHandler.class.getClassLoader().getResourceAsStream("config.properties");

            properties.load(inputStream);

            System.out.printf("Loaded %d properties%n", properties.size());

        } catch (IOException e) {
            System.out.printf("Failed to load configuration: %s%n", e);
            return;
        }

        try {
            this.serverKey = SecurityHelper.generateKeyFromPassword(properties.getProperty("binasPw"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void handleKerberosServerHandlerError(String clientError, String serverError) {
        System.out.println(serverError);
        throw new RuntimeException(clientError);
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        System.out.println("---------------------------- KerberosServerHandler: Handling message. ----------------------------");

        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (!outboundElement.booleanValue()) {
                //System.out.println("Receiving inbound SOAP message...");
                handleInBoundMessage(smc);
            } else {
                //System.out.println("Receiving outBound SOAP message...");
                buildOutBoundHeader(smc);

            }
        } catch (Exception e) {
            System.out.print("Caught exception in handleMessage: ");
            e.printStackTrace();
            System.out.println("Continue normal processing...");
        }

        //System.out.println("-------------------------- KerberosServerHandler: END Handling message. --------------------------");

        return true;
    }



    private void handleInBoundMessage(SOAPMessageContext smc)  {

        try {
            // get SOAP envelope
            SOAPMessage msg = smc.getMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            SOAPHeader sh = se.getHeader();

            //System.out.println("KerberosServerHandler: Validating the ticket");
            Ticket ticket  = getAndValidateTicket(sh);
            //System.out.println("KerberosServerHandler: the ticket is valid");

            //System.out.println("KerberosServerHandler: Validating the auth");
            Auth auth = getAndValidateAuth(sh, ticket);
            //System.out.println("KerberosServerHandler: the auth is valid");

            //Check if there has been a possible repeat attack
            String operation = msg.getSOAPBody().getFirstChild().getLocalName();
            lastRequestTime.putIfAbsent(auth.getX(), (long)-1);
            if(auth.getTimeRequest().getTime() < lastRequestTime.get(auth.getX()) &&
                    (operation == "rentBina" || operation == "returnBina")    ) {
                handleKerberosServerHandlerError("Binas: invalid request ",
                        "KerberosServerHandler: SECURITY WARNING repeat attack possibility");
            }


            smc.put(SESSION_KEY, ticket.getKeyXY());
            lastRequestTime.put(auth.getX(), auth.getTimeRequest().getTime());
            smc.put(REQUEST_TIME, new RequestTime(auth.getTimeRequest()));
            smc.put(CLIENT_NAME, ticket.getX());

            //System.out.println("KerberosServerHandler: OK");


        } catch (SOAPException e) {
            handleKerberosServerHandlerError("Binas: invalid request message",
                    "KerberosServerHandler: error trying to handle the SOAP message");
        }
    }

    private Ticket getAndValidateTicket(SOAPHeader sh)  {

        try {
            String s_ticket = sh.getElementsByTagNameNS("http://ws.binas.org/", "ticket").item(0).getTextContent();

            byte[] ticketDecodedBytes = Base64.getDecoder().decode(s_ticket);

            CipheredView ticketView = new CipheredView();
            ticketView.setData(ticketDecodedBytes);

            Ticket ticket = new Ticket(ticketView, serverKey);

            ticket.validate();


            if(ticket.getTime2().before(new Date())) {
                handleKerberosServerHandlerError("Binas: invalid request ",
                        "KerberosServerHandler: SECURITY WARNING ticket expired");
            } else if (ticket.getTime1().after(new Date())) {
                handleKerberosServerHandlerError("Binas: invalid request ",
                        "KerberosServerHandler: SECURITY WARNING system clock out of sync");
            }

            return ticket;
        } catch (KerbyException e) {
            handleKerberosServerHandlerError("Binas: invalid request ",
                    "KerberosServerHandler: error creating the ticket");
        }
        return null;
    }

    private Auth getAndValidateAuth(SOAPHeader sh, Ticket ticket) {


        try {
            String s_auth = sh.getElementsByTagNameNS("http://ws.binas.org/", "auth").item(0).getTextContent();
            byte[] decodedBytes = Base64.getDecoder().decode(s_auth);


            CipheredView authView = new CipheredView();
            authView.setData(decodedBytes);

            Auth auth = new Auth(authView, ticket.getKeyXY());
            auth.validate();


            if(auth.getTimeRequest().before(ticket.getTime1()) || auth.getTimeRequest().after(ticket.getTime2())) {
                handleKerberosServerHandlerError("Binas: invalid request ",
                        "KerberosServerHandler: SECURITY WARNING  time request outside bounds");
            } else if(auth.getTimeRequest().getTime() + this.timeLeeway < System.currentTimeMillis()) {
                handleKerberosServerHandlerError("Binas: invalid request ",
                        "KerberosServerHandler: SECURITY WARNING request too old");
            }
            return auth;
        } catch (KerbyException e) {
            e.printStackTrace();
        }
        return null;

    }


    private void buildOutBoundHeader(SOAPMessageContext smc) {


        try {
            //System.out.println("KerberosServerHandler: building the header");
            // get SOAP envelope
            SOAPMessage msg = smc.getMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            SOAPHeader sh = se.getHeader();

            //create the element
            Name name = se.createName("requestTime", "sec", "http://ws.binas.org/");
            SOAPHeaderElement element = sh.addHeaderElement(name);

            //System.out.println("KerberosServerHandler: getting the request time");
            Key sessionKey = (Key) smc.get(SESSION_KEY);
            RequestTime requestTime = (RequestTime) smc.get(REQUEST_TIME);

            //add the request time to the element
            //System.out.println("KerberosServerHandler: inserting the request time in the header");
            byte[] encodedBytes = Base64.getEncoder().encode(requestTime.cipher(sessionKey).getData());
            element.addTextNode(new String(encodedBytes));

        } catch (SOAPException e) {
            handleKerberosServerHandlerError("Binas: an unexpected internal error happened ",
                    "KerberosServerHandler: error trying to build the header");
        } catch (KerbyException e) {
            handleKerberosServerHandlerError("Binas: invalid request ",
                    "KerberosServerHandler: error trying to cipher the ticket");
        }

    }

    @Override
    public Set<QName> getHeaders() {
        return null;//TODO
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;//TODO
    }

    @Override
    public void close(MessageContext context) { }
}
