package example.ws.handler;

import org.w3c.dom.NodeList;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Set;

public class MACHandler implements SOAPHandler<SOAPMessageContext> {
    private static final String SESSION_KEY = "sessionKey";

    private static final String MAC = "mac";
    private static final String SEC = "sec";
    private static final String URI = "http://ws.binas.org/";

    private static final String MACPROVIDER = "HmacSHA512";


    public String computeMAC(String message, Key sessionKey) {

        byte [] byteKey = sessionKey.getEncoded();

        try {
            Mac sha512_HMAC = Mac.getInstance(MACPROVIDER);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, MACPROVIDER);
            sha512_HMAC.init(keySpec);
            byte [] mac_data = sha512_HMAC.doFinal(message.getBytes());
            return new String(mac_data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println("MACHandler: error trying to compute the MAC");
           throw new RuntimeException(e);
        }
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
            if (outboundElement.booleanValue()) { //Outbound
                System.out.println("MACHandler: Writing header to OUTbound SOAP message.");

                // get SOAP envelope
                Key sessionKey = (Key) context.get(SESSION_KEY);
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
                String MACToSend = computeMAC(request, sessionKey);
                byte[] encodedBytes = Base64.getEncoder().encode(MACToSend.getBytes());

                element.addTextNode(new String(encodedBytes));


            } else if(!outboundElement.booleanValue()){ //Inbound

                System.out.println("MACHandler: Reading header from INbound SOAP message...");

                Key sessionKey = (Key) context.get(SESSION_KEY);

                SOAPMessage msg = context.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                if (sh == null) {
                    System.out.println("MACHandler: Header not found.");
                    return true;
                }

                NodeList userIdNode = sh.getElementsByTagNameNS(URI, MAC);
                String valueString = userIdNode.item(0).getChildNodes().item(0).getNodeValue();

                byte[] decodedBytes = Base64.getDecoder().decode(valueString);



                String ReceivedMAC = new String(decodedBytes);

                String receivedRequest = getSoapBody(context);
                String computedMAC = computeMAC(receivedRequest, sessionKey);

                if(!ReceivedMAC.equals(computedMAC)){
                    throw new RuntimeException("MACHandler: SECURITY WARNING mismatch in calculated MAC and received MAC");
                }else {
                    System.out.println("MACHandler: OK Calculated MAC and received MAC are a match" );
                }
            }

        } catch (SOAPException | TransformerException e) {
            throw new RuntimeException("MACHandler: error trying to process the message");
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

}
