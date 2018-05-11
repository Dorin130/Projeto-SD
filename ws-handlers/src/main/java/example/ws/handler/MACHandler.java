package example.ws.handler;

import org.w3c.dom.NodeList;
import pt.ulisboa.tecnico.sdis.kerby.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;
import java.util.Set;

public class MACHandler implements SOAPHandler<SOAPMessageContext> {
    Properties properties;

    private static final String SESSION_KEY = "sessionKey";

    public static Boolean isClient = false; //Need to know if the handler is being executed in the client Side or in the server side

    private static final String MAC = "mac";
    private static final String SEC = "sec";
    private static final String URI = "http://ws.binas.org/";

    private static final String HASHALGORITHM = "SHA-256";

    public MACHandler() {
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
    }


    public String computeMAC(String message, Key sessionKey) throws NoSuchAlgorithmException, InvalidKeyException {
        MessageDigest messageDigest = MessageDigest.getInstance(HASHALGORITHM);
        byte[] passBytes = message.getBytes();
        byte[] mac = messageDigest.digest(passBytes);




/*
        byte [] byteKey = sessionKey.getEncoded();
        final String HMAC_SHA512 = "HmacSHA512";
        Mac sha512_HMAC = Mac.getInstance(HMAC_SHA512);
        SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
        sha512_HMAC.init(keySpec);
        byte [] mac_data = sha512_HMAC.
                doFinal(message.getBytes());
        //result = Base64.encode(mac_data);
        String result = new String(mac_data);
        System.out.println("hey1");
        System.out.println(result);
        System.out.println("hey1");

        mac = messageDigest.digest(passBytes);*/


        return new String(mac);

    }
    public String decipherMAC(byte[] encriptedBytes, Key sessionKey) throws KerbyException {
        CipheredView c = new CipheredView();
        c.setData(encriptedBytes);
        CipheredView decoded  = SecurityHelper.decipher(CipheredView.class, c, sessionKey);
        return new String(decoded.getData());

    }

    public CipheredView computeAndCipherMAC(String message, Key sessionKey) throws NoSuchAlgorithmException, KerbyException, InvalidKeyException {

        String calculatedMac = computeMAC(message, sessionKey);
        CipheredView wrapper = new CipheredView();
        wrapper.setData(calculatedMac.getBytes());

        return SecurityHelper.cipher(CipheredView.class, wrapper, sessionKey);
    }

    private String getSoapBody(SOAPMessageContext smc) throws SOAPException, TransformerException {
        SOAPBody element = smc.getMessage().getSOAPBody();
        DOMSource source = new DOMSource(element);
        StringWriter stringResult = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
        return stringResult.toString();
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

                Key sessionKey = (Key) context.get(SESSION_KEY);

                System.out.println(sessionKey.getEncoded());

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

                Name name = se.createName(MAC, SEC, URI);
                SOAPHeaderElement element = sh.addHeaderElement(name);

                //Extract SOAP body to get,
                String request = getSoapBody(context);

                //Create the cyphered request to put on the Header
                CipheredView MACToSend = computeAndCipherMAC(request, sessionKey);
                byte[] encodedBytes = Base64.getEncoder().encode(MACToSend.getData());


                element.addTextNode(new String(encodedBytes));


            } else if(!outboundElement.booleanValue() && !isClient){

                System.out.println("Reading header from INbound SOAP message...");


                SOAPMessage msg = context.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                if (sh == null) {
                    System.out.println("Header not found.");
                    return true;
                }

                NodeList userIdNode = sh.getElementsByTagNameNS("http://ws.binas.org/", MAC);
                String valueString = userIdNode.item(0).getChildNodes().item(0).getNodeValue();

                byte[] decodedBytes = Base64.getDecoder().decode(valueString);

                Key sessionKey = (Key) context.get(SESSION_KEY);

                String decodedReceivedHash = decipherMAC(decodedBytes, sessionKey);

                String receivedRequest = getSoapBody(context);
                String computedMAC = computeMAC(receivedRequest, sessionKey);

                if(!decodedReceivedHash.equals(computedMAC)){
                    System.out.println("MACHandler: SECURITY WARNING mismatch in calculated MAC and received MAC");
                    throw new RuntimeException();
                }else {
                    System.out.println("MACHandler: OK Calculated MAC and received MAC are a match" );
                }
            }

        } catch (Exception e) {
            System.out.print("Caught exception in handleMessage: ");
            System.out.println(e);
            e.printStackTrace();
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

    public static void setIsClient(Boolean isClient) {
        MACHandler.isClient = isClient;
    }
}
