package example.ws.handler;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pt.ulisboa.tecnico.sdis.kerby.*;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClientException;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;


public class KerberosClientHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String SESSION_KEY = "sessionKey";
    private static final String TIME_REQUEST = "timeRequest";

    Properties properties;
    SecureRandom secureRandom;
    static TicketCollection ticketCollection;
    KerbyClient kerbyClient;
    CipherClerk cipherClerk;
    Key clientKey;


    public KerberosClientHandler() {
        super();
        this.properties = new Properties();
        this.secureRandom = new SecureRandom();
        this.ticketCollection = new TicketCollection();
        this.cipherClerk = new CipherClerk();

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
            this.clientKey = SecurityHelper.generateKeyFromPassword(properties.getProperty("pass"));
            this.kerbyClient = new KerbyClient(properties.getProperty("kerbyWs"));
        } catch (KerbyClientException e) {
            throw new RuntimeException("KerberosClientHandler: error trying to initialize the kerby client");
        }catch( NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("KerberosClientHandler: error trying to initialize the handler");
        }
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        System.out.println("---------------------------- KerberosClientHandler: Handling message. ----------------------------");

        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (outboundElement.booleanValue()) {
                //System.out.println("Receiving outbound SOAP message...");
                handleOutboundMessage(smc);

            } else {
                //System.out.println("Receiving inbound SOAP message...");
                handleInboundMessage(smc);
            }
        } catch (KerbyException e) {
            throw new RuntimeException("KerberosClientHandler: error trying to process the message");
        } catch (BadTicketRequest_Exception e) {
            throw new RuntimeException("KerberosClientHandler: error trying to request a new ticket");
        }

        //System.out.println("---------------------------KerberosClientHandler: END Handling message. ---------------------------");

        return true;
    }

    private void handleOutboundMessage(SOAPMessageContext smc) throws BadTicketRequest_Exception, KerbyException {
        boolean shouldVerifyNonce = false;
        long nonce = 0;

        //get session key from ticketCollection
        SessionKeyAndTicketView sktv = ticketCollection.getTicket(properties.getProperty("binas"));

        //build new ticket if first time or the other on as expired
        if (sktv == null) {
            nonce = secureRandom.nextLong();
            shouldVerifyNonce = true;
            long expirationTime = (System.currentTimeMillis() + Integer.parseInt(properties.getProperty("ticketTime")) * 1000);

            sktv = kerbyClient.requestTicket(properties.getProperty("user"),
                    properties.getProperty("binas"), nonce, Integer.parseInt(properties.getProperty("ticketTime")));

            ticketCollection.storeTicket(properties.getProperty("binas"), sktv, expirationTime);

        }

        //build session key, ticket and auth
        //System.out.println("KerberosClientHandler: Creating the SessionKey");
        SessionKey sessionKey = new SessionKey(sktv.getSessionKey(), clientKey);
        //System.out.println("KerberosClientHandler: Creating the Ticket");
        CipheredView ticketView = sktv.getTicket();
        //System.out.println("KerberosClientHandler: Creating the Auth");
        Auth auth = new Auth(properties.getProperty("user"), new Date());
        CipheredView authView = auth.cipher(sessionKey.getKeyXY());


        if (shouldVerifyNonce && sessionKey.getNounce() != nonce) {
            System.out.println("SECURITY WARNING: nonce mismatch");
            throw new RuntimeException("SECURITY WARNING: nonce mismatch");
        }

        //Prepare the header to be sent
        //System.out.println("KerberosClientHandler: Building the header");
        buildHeader(smc, ticketView, authView);

        //insert useful objects in message context
        smc.put(SESSION_KEY, sessionKey.getKeyXY());
        smc.put(TIME_REQUEST, auth.getTimeRequest());
    }

    private void handleInboundMessage(SOAPMessageContext smc) throws KerbyException {
        String s_requestTime = extractRequestTimeFromSOAPMessage(smc);

        byte[] reqTimeDecodedBytes = Base64.getDecoder().decode(s_requestTime);
        CipheredView reqTimeCV = new CipheredView();
        reqTimeCV.setData(reqTimeDecodedBytes);

        RequestTime requestTime = new RequestTime(reqTimeCV, (Key) smc.get (SESSION_KEY));

        Date timeOfRequest = (Date) smc.get(TIME_REQUEST);

        //System.out.println("KerberosClientHandler: Validating received request time");
        if(requestTime.getTimeRequest().getTime() != timeOfRequest.getTime()) {
            throw new RuntimeException("KerberosClientHandler: SECURITY WARNING Wrong request time");
        }
        System.out.println("KerberosClientHandler: OK");

    }

    private void buildHeader(SOAPMessageContext context,CipheredView ticketView, CipheredView authView) {
        try {
            // get SOAP envelope
            SOAPMessage msg = context.getMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();

            // add header
            SOAPHeader sh = se.getHeader();
            if (sh == null)
                sh = se.addHeader();

            //insert ticket in the header
            //System.out.println("KerberosClientHandler: Inserting the ticket in the header");
            buildHeaderElement(se, sh, ticketView, "ticket", "sec", "http://ws.binas.org/");

            //insert auth in the header
            //System.out.println("KerberosClientHandler: Inserting the auth in the header");
            buildHeaderElement(se, sh, authView, "auth", "sec", "http://ws.binas.org/");

        } catch (SOAPException e) {
            throw new RuntimeException("KerberosClientHandler: error trying to build the header");
        }
    }

    private void  buildHeaderElement(SOAPEnvelope se, SOAPHeader sh, CipheredView cipheredView,
                                     String localName, String prefix, String uri) throws SOAPException {

        // add header element (name, namespace prefix, namespace)
        Name name = se.createName(localName, prefix, uri);
        SOAPHeaderElement element = sh.addHeaderElement(name);

        // add header element value
        byte[] encodedBytes = Base64.getEncoder().encode(cipheredView.getData());
        element.addTextNode(new String(encodedBytes));

    }

    private String extractRequestTimeFromSOAPMessage(SOAPMessageContext smc) {

        try {
            // get SOAP envelope
            SOAPMessage msg = smc.getMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            SOAPHeader sh = se.getHeader();

            NodeList elementList = sh.getElementsByTagNameNS("http://ws.binas.org/", "requestTime");
            if(elementList != null && elementList.getLength() > 0) {
                Node node = elementList.item(0);
                if(node != null) {
                    return node.getTextContent();
                }
            }
            return null;

        } catch (SOAPException e) {
            throw new RuntimeException("KerberosClientHandler: error trying to extract the request time from the SOAP message");
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
    public void close(MessageContext context) {
        //TODO
    }
}
