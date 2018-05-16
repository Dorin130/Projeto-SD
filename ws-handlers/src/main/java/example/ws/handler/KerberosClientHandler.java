package example.ws.handler;

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
            // variant for non-static methods:
            // InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");

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
            e.printStackTrace();
            throw new RuntimeException();
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
        System.out.println("---------------------------- KerberosClientHandler: Handling message. ----------------------------");

        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (outboundElement.booleanValue()) {
                boolean shouldVerifyNonce = false;
                long nonce = 0;
                System.out.println("Writing ticket to OUTbound SOAP message...");

                System.out.println("- Generated nonce:");

                //System.out.println(nonce);

                SessionKeyAndTicketView sktv = ticketCollection.getTicket(properties.getProperty("binas"));

                if (sktv == null) {
                    nonce = secureRandom.nextLong();
                    shouldVerifyNonce = true;
                    long expirationTime = (System.currentTimeMillis() + Integer.parseInt(properties.getProperty("ticketTime")) * 1000);

                    sktv = kerbyClient.requestTicket(properties.getProperty("user"),
                            properties.getProperty("binas"), nonce, Integer.parseInt(properties.getProperty("ticketTime")));

                    ticketCollection.storeTicket(properties.getProperty("binas"), sktv, expirationTime);

                }

                System.out.println("- SessionKey:");
                SessionKey sessionKey = new SessionKey(sktv.getSessionKey(), clientKey);
                System.out.println(sessionKey.toString());


                CipheredView ticketView = sktv.getTicket();

                System.out.println("- ticket nonce:");
                System.out.println(sessionKey.getNounce());

                if (shouldVerifyNonce && sessionKey.getNounce() != nonce) {
                    System.out.println("SECURITY WARNING: nonce mismatch");
                    throw new RuntimeException("SECURITY WARNING: nonce mismatch");
                } 

                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                // add header
                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();

                // add header element (name, namespace prefix, namespace)
                Name name = se.createName("ticket", "sec", "http://ws.binas.org/");
                SOAPHeaderElement element = sh.addHeaderElement(name);

                // add header element value
                System.out.println(" - Sent Ticket:");
                byte[] encodedBytes = Base64.getEncoder().encode(ticketView.getData());
                element.addTextNode(new String(encodedBytes));
                System.out.println(new String(encodedBytes));

                name = se.createName("auth", "sec", "http://ws.binas.org/");
                element = sh.addHeaderElement(name);

                // add header element value
                System.out.println(" - Generated Auth:");
                Auth auth = new Auth(properties.getProperty("user"), new Date());
                System.out.println(auth.authToString());

                System.out.println(" - Sent Auth:");
                encodedBytes = Base64.getEncoder().encode(auth.cipher(sessionKey.getKeyXY()).getData());
                element.addTextNode(new String(encodedBytes));
                System.out.println(new String(encodedBytes));

                smc.put(SESSION_KEY, sessionKey.getKeyXY());
                smc.put(TIME_REQUEST, auth.getTimeRequest());


                // put header in a property context
                //smc.put(CONTEXT_SESSION_KEY, sessionKey);
                // set property scope to application client/server class can
                // access it
                //smc.setScope(CONTEXT_SESSION_KEY, MessageContext.Scope.APPLICATION);
                //smc.setScope("test", MessageContext.Scope.APPLICATION);

            } else {
                System.out.println("Receiving inbound SOAP message...");

                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

               // System.out.println(" - Got Ciphered Request Time:");
                String s_requestTime = sh.getElementsByTagNameNS("http://ws.binas.org/", "requestTime").item(0).getTextContent();
                //System.out.println(s_requestTime);

                //System.out.println(" - Got  Request Time:");
                byte[] reqTimeDecodedBytes = Base64.getDecoder().decode(s_requestTime);
                CipheredView reqTimeCV = new CipheredView();
                reqTimeCV.setData(reqTimeDecodedBytes);

                RequestTime requestTime = new RequestTime(reqTimeCV, (Key) smc.get(SESSION_KEY));
                //System.out.println(requestTime.requestTimeToString());
                Date timeOfRequest = (Date) smc.get(TIME_REQUEST);


                if(requestTime.getTimeRequest().getTime() != timeOfRequest.getTime()) {
                    System.out.println("SECURITY WARNING: Wrong request time");
                    throw new RuntimeException("SECURITY WARNING: Wrong request time");
                }

            }
        } catch (Exception e) {
            System.out.print("Caught exception in handleMessage: ");
            System.out.println(e);
            System.out.println("Continue normal processing...");
        }

        System.out.println("---------------------------KerberosClientHandler: END Handling message. ---------------------------");

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
