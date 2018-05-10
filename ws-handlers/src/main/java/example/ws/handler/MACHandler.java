package example.ws.handler;

import pt.ulisboa.tecnico.sdis.kerby.CipheredView;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;

public class MACHandler implements SOAPHandler<SOAPMessageContext> {
    public static String SESSIONKEY;

    public static final String MAC = "MAC";

    public String computeMAC(String message) {
        return null; //TODO
    }

    public CipheredView computeAndCypherMAC(String message) {
        return null; //TODO
    }

    public boolean compareMAC(String MACReceived, String message) {
        return MACReceived == computeMAC(message);
    }

    public String getHeaderValueByName(SOAPMessageContext context, String name) {
        // get first header element
        Name macName = context.createName(name);
        Iterator<?> it = context.getChildElements(macName);
        // check header element
        if (!it.hasNext()) {
            System.out.println("Header element not found.");
            return null;
        }
        SOAPElement element = (SOAPElement) it.next();

        // get header element value
        String valueString = element.getValue();
        int value = Integer.parseInt(valueString);
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
            if (outboundElement.booleanValue()) { //clientSide
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
                Name name = se.createName(MAC);
                SOAPHeaderElement element = sh.addHeaderElement(name);


                CipheredView MACToSend = computeAndCypherMAC("");


                // add header element value
                int value = 22;
                String valueString = Integer.toString(value);
                element.addTextNode(valueString);

            } else {
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
                context.put(CONTEXT_PROPERTY, value);
                // set property scope to application client/server class can
                // access it
                context.setScope(CONTEXT_PROPERTY, MessageContext.Scope.APPLICATION);

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

    public static void setSESSIONKEY(String SESSIONKEY) {
        MACHandler.SESSIONKEY = SESSIONKEY;
    }

}
