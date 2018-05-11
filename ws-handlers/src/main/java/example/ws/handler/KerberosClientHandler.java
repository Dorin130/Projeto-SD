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
    Properties properties;
    SecureRandom secureRandom;
    TicketCollection ticketCollection;
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
        System.out.println("KerberosClientHandler: Handling message.");

        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (outboundElement.booleanValue()) {
                System.out.println("Writing ticket to OUTbound SOAP message...");

                long nonce = secureRandom.nextLong();

                SessionKeyAndTicketView sktv = ticketCollection.getTicket(properties.getProperty("binas"));

                if (sktv == null) {
                    long expirationTime = (new Date()).getTime() + Integer.parseInt(properties.getProperty("ticketTime")) * 1000;

                    sktv = kerbyClient.requestTicket(properties.getProperty("user"),
                            properties.getProperty("binas"), nonce, Integer.parseInt(properties.getProperty("ticketTime")));

                    ticketCollection.storeTicket(properties.getProperty("binas"), sktv, expirationTime);
                }

                SessionKey sessionKey = new SessionKey(sktv.getSessionKey(), clientKey);

                CipheredView ticketView = sktv.getTicket();

                if (sessionKey.getNounce() != nonce) {
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
                byte[] encodedBytes = Base64.getEncoder().encode(ticketView.getData());
                element.addTextNode(new String(encodedBytes));

                System.out.println("Writing auth to OUTbound SOAP message...");

                name = se.createName("auth", "sec", "http://ws.binas.org/");
                element = sh.addHeaderElement(name);

                // add header element value
                Auth auth = new Auth(properties.getProperty("user"), new Date());

                encodedBytes = Base64.getEncoder().encode(auth.cipher(sessionKey.getKeyXY()).getData());
                element.addTextNode(new String(encodedBytes));

            } else {
                System.out.println("KerberosClientHandler ignores...");

            }
        } catch (Exception e) {
            System.out.print("Caught exception in handleMessage: ");
            System.out.println(e);
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
