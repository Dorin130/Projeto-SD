package example.ws.handler;

import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class KerberosClientHandler implements SOAPHandler<SOAPMessageContext> {
    private static String USER;
    private static String PASS;
    Properties properties;
    SecureRandom SecureRandom;

    public static final String CONTEXT_PROPERTY = "user.ticket";

    public KerberosClientHandler() {
        super();
        this.properties = new Properties();
        this.SecureRandom = new SecureRandom();
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

                KerbyClient kerbyClient = new KerbyClient(properties.getProperty("kerbyWs"));
                // add header element value
                int value = 22;
                String valueString = Integer.toString(value);
                element.addTextNode(valueString);

            } else {
                System.out.println("Reading header from INbound SOAP message...");

                // get SOAP envelope header
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                // check header
                if (sh == null) {
                    System.out.println("Header not found.");
                    return true;
                }

                // get first header element
                Name name = se.createName("myHeader", "d", "http://demo");
                Iterator<?> it = sh.getChildElements(name);
                // check header element
                if (!it.hasNext()) {
                    System.out.println("Header element not found.");
                    return true;
                }
                SOAPElement element = (SOAPElement) it.next();

                // get header element value
                String valueString = element.getValue();
                int value = Integer.parseInt(valueString);

                // print received header
                System.out.println("Header value is " + value);

                // put header in a property context
                smc.put(CONTEXT_PROPERTY, value);
                // set property scope to application client/server class can
                // access it
                smc.setScope(CONTEXT_PROPERTY, MessageContext.Scope.APPLICATION);

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

    public static void setUSER(String user) {
        USER = user;
    }

    public static void setPASS(String pass) {
        PASS = pass;
    }
}
