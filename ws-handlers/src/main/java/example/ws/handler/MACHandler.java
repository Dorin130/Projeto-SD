package example.ws.handler;

import org.w3c.dom.NodeList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;

public class MACHandler implements SOAPHandler<SOAPMessageContext> {
    private static final String SESSION_KEY = "sessionKey";

    private static final String MAC = "mac";
    private static final String SEC = "sec";
    private static final String URI = "http://ws.binas.org/";

    private static final String MACPROVIDER = "HmacSHA512";



    @Override
    public boolean handleMessage(SOAPMessageContext context) {

        System.out.println("---------------------------- MACHandler: Handling message. ----------------------------");

        Boolean outboundElement = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            if (outboundElement.booleanValue()) { //Outbound
                //System.out.println("MACHandler: Writing header to OUTbound SOAP message.");
                handleOutboundMessage(context);

            } else if(!outboundElement.booleanValue()){ //Inbound
                handleInboundMessage(context);
            }

        } catch (SOAPException | TransformerException e) {
            throw new RuntimeException("MACHandler: error trying to process the message");
        }
        //System.out.println("---------------------------- MACHandler: END Handling message. ----------------------------");

        return true;
    }

    private void handleOutboundMessage(SOAPMessageContext context) throws TransformerException, SOAPException {
        // get SOAP envelope
        Key sessionKey = (Key) context.get(SESSION_KEY);

        //build the header to insert the MAC
        //System.out.println("MACHandler: building the header");
        SOAPHeaderElement MACHeaderElement = buildHeaderElement(context);

        //Extract SOAP body to get the request
        String request = getSoapBody(context);

        //System.out.println("MACHandler: computing the MAC");
        byte[] MACToSend = computeMAC(request, sessionKey);
        byte[] encodedBytes = Base64.getEncoder().encode(MACToSend);

        //System.out.println("MACHandler: Inserting the MAC in the header");
        MACHeaderElement.addTextNode(new String(encodedBytes));
    }

    private void handleInboundMessage(SOAPMessageContext context) throws SOAPException, TransformerException {
        //System.out.println("MACHandler: Reading header from INbound SOAP message...");

        Key sessionKey = (Key) context.get(SESSION_KEY);

        SOAPHeader sh = getHeader( context);
        if(sh == null) {
            throw new RuntimeException("MACHandler: SECURITY WARNING missing headers in inbound message");
        }


        //extract the MAC from the header element
        byte [] receivedMAC = extractMACfromSOAPHeader(sh);

        String receivedRequest = getSoapBody(context);
        //System.out.println("MACHandler: computing the MAC");
        byte [] computedMAC = computeMAC(receivedRequest, sessionKey);


        //System.out.println("MACHandler: validating received MAC");
        if(!Arrays.equals(receivedMAC, computedMAC)){
            throw new RuntimeException("MACHandler: SECURITY WARNING mismatch in calculated MAC and received MAC");
        }else {
            //System.out.println("MACHandler: OK Calculated MAC and received MAC are a match" );
        }
    }

    private SOAPHeaderElement buildHeaderElement(SOAPMessageContext context) {

        try {

            SOAPMessage msg = context.getMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();

            // add header
            SOAPHeader sh = getHeader(context);
            if (sh == null)
                sh = se.addHeader();

            // add header element
            Name name = se.createName(MAC, SEC, URI);
            return sh.addHeaderElement(name);

        } catch (SOAPException e) {
            throw new RuntimeException("MACHandler: error trying to build the header element");
        }
    }

    private String getSoapBody(SOAPMessageContext smc) throws SOAPException, TransformerException {
        SOAPBody element = smc.getMessage().getSOAPBody();
        DOMSource source = new DOMSource(element);
        StringWriter stringResult = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
        return stringResult.toString();
    }


    private SOAPHeader getHeader(SOAPMessageContext context) throws SOAPException {
        SOAPMessage msg = context.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPHeader sh = se.getHeader();

        if (sh == null) {
            System.out.println("MACHandler: Header not found.");
            return null;
        }
        return sh;
    }

    private byte [] extractMACfromSOAPHeader(SOAPHeader sh) {
        NodeList userIdNode = sh.getElementsByTagNameNS(URI, MAC);

        if(userIdNode == null || userIdNode.getLength() == 0 ||
                (userIdNode.item(0) != null && userIdNode.item(0).getChildNodes().getLength() == 0)) {
            throw new RuntimeException("MACHandler: SECURITY WARNING missing MAC in inbound message");
        }

        String macNode = userIdNode.item(0).getChildNodes().item(0).getNodeValue();

        byte[] decodedBytes = Base64.getDecoder().decode(macNode);

        return decodedBytes;

    }

    public byte [] computeMAC(String message, Key sessionKey) {

        byte [] byteKey = sessionKey.getEncoded();

        try {
            Mac sha512_HMAC = Mac.getInstance(MACPROVIDER);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, MACPROVIDER);
            sha512_HMAC.init(keySpec);
            byte [] mac_data = sha512_HMAC.doFinal(message.getBytes());
            return mac_data;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("MACHandler: error trying to compute the MAC");
        }
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {return false; }

    @Override
    public Set<QName> getHeaders() {return null; }

    @Override
    public void close(MessageContext context) {   }

}
