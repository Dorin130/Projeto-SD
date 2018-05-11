package example.ws.handler;

import pt.ulisboa.tecnico.sdis.kerby.*;


import javax.crypto.Cipher;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.security.Key;
import java.time.LocalDate;
import java.util.Set;

import javax.xml.soap.*;

import java.util.*;


import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClientException;


import java.io.IOException;
import java.io.InputStream;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ConcurrentHashMap;


public class KerberosServerHandler implements SOAPHandler<SOAPMessageContext> {
    Properties properties;
    Key serverKey;
    long timeLeeway = 1000;
    static Map<String, Key> sessionKeys = new ConcurrentHashMap<>();
    static Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    public KerberosServerHandler() {
        super();
        this.properties = new Properties();

        // load configuration properties
        try {
            InputStream inputStream = KerberosClientHandler.class.getClassLoader().getResourceAsStream("config.properties");
            // variant for non-static methods:
            // InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");

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

    @Override
    public Set<QName> getHeaders() {
        return null;//TODO
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        System.out.println("KerberosServerHandler: Handling message.");

        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (!outboundElement.booleanValue()) {
                System.out.println("Receiving inbound SOAP message...");

                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                String s_ticket = sh.getElementsByTagNameNS("http://ws.binas.org/", "ticket").item(0).getTextContent();
                byte[] ticketDecodedBytes = Base64.getDecoder().decode(s_ticket);

                CipheredView ticketView = new CipheredView();
                ticketView.setData(ticketDecodedBytes);

                Ticket ticket = new Ticket(ticketView, serverKey);
                MACHandler.setSESSIONKEY(ticket.getKeyXY());
                ticket.validate(); //necessary?

                if(ticket.getTime2().before(new Date())) {
                    System.out.println("SECURITY WARNING: ticket expired");
                    throw new RuntimeException("SECURITY WARNING: ticket expired");
                } else if (ticket.getTime1().after(new Date())) {
                    System.out.println("SECURITY WARNING: system clock out of sync");
                    throw new RuntimeException("SECURITY WARNING: system clock out of sync");
                }

                String s_auth = sh.getElementsByTagNameNS("http://ws.binas.org/", "auth").item(0).getTextContent();
                byte[] decodedBytes = Base64.getDecoder().decode(s_auth);

                CipheredView authView = new CipheredView();
                authView.setData(decodedBytes);

                Auth auth = new Auth(authView, ticket.getKeyXY());
                auth.validate();  //necessary?

                if(auth.getTimeRequest().before(ticket.getTime1()) || auth.getTimeRequest().after(ticket.getTime2())) {
                    System.out.println("SECURITY WARNING: time request outside bounds");
                    throw new RuntimeException("SECURITY WARNING: time request outside bounds");
                } else if(auth.getTimeRequest().getTime() + this.timeLeeway < new Date().getTime()) {
                    System.out.println("SECURITY WARNING: request too old");
                    throw new RuntimeException("SECURITY WARNING: request too old");
                }

                /*
                QName opn = (QName) smc.get(MessageContext.WSDL_OPERATION);
                opn.getLocalPart();
                */
                String operation = msg.getSOAPBody().getFirstChild().getLocalName();
                System.out.println(operation);

                lastRequestTime.putIfAbsent(auth.getX(), (long)-1);
                if(auth.getTimeRequest().getTime() < lastRequestTime.get(auth.getX()) &&
                        (operation == "rentBina" || operation == "returnBina")    ) {
                    System.out.println("SECURITY WARNING: repeat attack possibility");
                    throw new RuntimeException("SECURITY WARNING: repeat attack possibility");
                }
                sessionKeys.put(auth.getX(), ticket.getKeyXY());
                lastRequestTime.put(auth.getX(), auth.getTimeRequest().getTime());

                //for authentication handler
                Name name = se.createName("user", "sec", "http://ws.binas.org/");
                sh.addHeaderElement(name).addTextNode(auth.getX());

            } else {
                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                System.out.println("Writing auth (reply) to OUTbound SOAP message...");

                Name name = se.createName("auth", "sec", "http://ws.binas.org/");
                SOAPHeaderElement element = sh.addHeaderElement(name);

                String user = ""; //TODO THIS
                CipheredView authReply = new CipheredView();
                //byte[] encodedBytes = Base64.getEncoder().encode(Long.toString(lastRequestTime.get(user)).getBytes());
                authReply.setData(Long.toString(lastRequestTime.get(user)).getBytes());

                authReply = SecurityHelper.cipher(CipheredView.class, authReply, sessionKeys.get(user));

                byte[] encodedBytes = Base64.getEncoder().encode(authReply.getData());
                element.addTextNode(new String(encodedBytes));

                System.out.println("KerberosClientHandler ignores...");

            }
        } catch (Exception e) {
            System.out.print("Caught exception in handleMessage: ");
            e.printStackTrace();
            System.out.println("Continue normal processing...");
        }

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;//TODO
    }

    @Override
    public void close(MessageContext context) {
        //TODO
    }
}
