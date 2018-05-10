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
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class KerberosClientHandler implements SOAPHandler<SOAPMessageContext> {
    Properties properties;
    SecureRandom secureRandom;
    TicketCollection ticketCollection;
    KerbyClient kerbyClient;


    public static final String CONTEXT_PROPERTY = "user.ticket";

    public KerberosClientHandler() {
        super();
        this.properties = new Properties();
        this.secureRandom = new SecureRandom();
        this.ticketCollection = new TicketCollection();
        try {
            this.kerbyClient = new KerbyClient(properties.getProperty("kerbyWs"));
        } catch (KerbyClientException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
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
                System.out.println("Writing header to OUTbound SOAP message...");

                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                // add header
                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();

                // add header element (name, namespace prefix, namespace)
                Name name = se.createName("ticket");
                SOAPHeaderElement element = sh.addHeaderElement(name);

                long nonce = secureRandom.nextLong();

                SessionKeyAndTicketView sktv = ticketCollection.getTicket(properties.getProperty("binas"));

                if (sktv == null) {
                    long expirationTime = (new Date()).getTime() + Integer.parseInt(properties.getProperty("ticketTime")) * 1000;

                    sktv = kerbyClient.requestTicket(properties.getProperty("user"),
                            properties.getProperty("binas"), nonce, Integer.parseInt(properties.getProperty("ticketTime")));

                    ticketCollection.storeTicket(properties.getProperty("binas"), sktv, expirationTime);
                }

                SessionKey sessionKey = new SessionKey(sktv.getSessionKey(),
                        SecurityHelper.generateKeyFromPassword(properties.getProperty("pass")));

                CipheredView ticketView = sktv.getTicket();

                if (sessionKey.getNounce() != nonce) {
                    System.out.println("SECURITY WARNING: nonce mismatch");
                    throw new RuntimeException("SECURITY WARNING: nonce mismatch");
                }

                String valueString = "BLA";
                element.addTextNode(valueString);

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
