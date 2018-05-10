package example.ws.handler;

import pt.ulisboa.tecnico.sdis.kerby.CipherClerk;
import pt.ulisboa.tecnico.sdis.kerby.CipheredView;
import pt.ulisboa.tecnico.sdis.kerby.KerbyException;
import pt.ulisboa.tecnico.sdis.kerby.SecurityHelper;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class MACHandler implements SOAPHandler<SOAPMessageContext> {
    Properties properties;
    CipherClerk cipherClerk;
    SecurityHelper securityHelper;

    public static Key SESSIONKEY;

    public static Boolean isClient = false; //Need to know if the handler is being executed in the client Side or in the server side

    private static final String MAC = "mac";

    private static final String HASHALGORITHM = "SHA-256";

    public MACHandler() {
        this.properties = new Properties();
        this.cipherClerk = new CipherClerk();
        this.securityHelper = new SecurityHelper();

        // load configuration properties
        try {
            InputStream inputStream = KerberosClientHandler.class.getClassLoader().getResourceAsStream("config.properties");
            properties.load(inputStream);

            System.out.printf("Loaded %d properties%n", properties.size());

        } catch (IOException e) {
            System.out.printf("Failed to load configuration: %s%n", e);
            return;
        }
    }


    public String computeMAC(String message) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(HASHALGORITHM);
        byte[] passBytes = message.getBytes();
        byte[] mac = messageDigest.digest(passBytes);
        return new String(mac);

    }

    public CipheredView computeAndCipherMAC(String message) throws NoSuchAlgorithmException, KerbyException {
        String calculatedMac = computeMAC(message);

        return SecurityHelper.cipher(String.class, calculatedMac, SESSIONKEY);
    }

    public boolean compareMAC(String MACReceived, String message) throws NoSuchAlgorithmException {
        return MACReceived.equals(computeMAC(message));
    }

    private String extractContentFromContextToString(SOAPMessageContext context) throws SOAPException, TransformerException {

        Source request = context.getMessage().getSOAPPart().getContent();
        StringWriter sr = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(request, new StreamResult(sr));
        return sr.toString();
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {

       System.out.println("MACHandler: Handling message.");

        Boolean outboundElement = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (outboundElement.booleanValue() && isClient) { //clientSide

                System.out.println("Writing header to OUTbound SOAP message...");

                // get SOAP envelope
                SOAPMessage msg = context.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                // add header

                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();

                // add header element
                Name name = se.createName(MAC, "sec", "http://ws.binas.org/");
                SOAPHeaderElement element = sh.addHeaderElement(name);


                //Extract SOAP body to get,
                String request = extractContentFromContextToString(context);

                //Create the cyphered request to put on the Header
                CipheredView MACToSend = computeAndCipherMAC(request);

                byte[] encodedBytes = Base64.getEncoder().encode(MACToSend.getData());

                element.addTextNode(new String(encodedBytes));


            } else if(!outboundElement.booleanValue() && !isClient){
                System.out.println("Reading header from INbound SOAP message...");


                // get SOAP envelope header
                SOAPMessage msg = context.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                // check header
                if (sh == null) {
                    System.out.println("Header not found.");
                    return true;
                }

                // get first header element
                Name name = se.createName(MAC, "sec", "http://ws.binas.org/");
                Iterator<?> it = sh.getChildElements(name);
                // check header element
                if (!it.hasNext()) {
                    System.out.println("Header element not found.");
                    return true;
                }
                SOAPElement element = (SOAPElement) it.next();

                // get header element value
                String valueString = element.getValue();

                byte[] encodedBytes = Base64.getDecoder().decode(valueString);
                CipheredView cipheredMac = new CipheredView();
                cipheredMac.setData(encodedBytes);
                String receivedMac = SecurityHelper.decipher(String.class, cipheredMac, SESSIONKEY );


                String receivedRequest = extractContentFromContextToString(context);

                if(!compareMAC(receivedMac, computeMAC(receivedRequest)))
                    throw new RuntimeException();


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
        return false;
    }

    @Override
    public void close(MessageContext context) {

    }

    public static void setSESSIONKEY(Key SESSIONKEY) {
        MACHandler.SESSIONKEY = SESSIONKEY;
    }

}
