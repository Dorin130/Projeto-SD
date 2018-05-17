package example.ws.handler;


import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import java.util.Set;

public class EvilHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public boolean handleMessage(SOAPMessageContext context) {

        System.out.println("---------------------------- MACHandler: Handling message. ----------------------------");

        Boolean outboundElement = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            if (outboundElement.booleanValue()) { //Outbound
                //System.out.println("MACHandler: Writing header to OUTbound SOAP message.");
                handleOutboundMessage(context);

            }

        } catch (SOAPException | TransformerException e) {
            throw new RuntimeException("MACHandler: error trying to process the message");
        }
        //System.out.println("---------------------------- MACHandler: END Handling message. ----------------------------");

        return true;
    }

    private void handleOutboundMessage(SOAPMessageContext context) throws TransformerException, SOAPException {
        SOAPBodyElement el = build(context);

        el.addTextNode(new String("more evil stuff"));
    }

    private SOAPBodyElement build(SOAPMessageContext context) {

        try {

            SOAPMessage msg = context.getMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            SOAPBody sb = se.getBody();

            // add header element
            Name name = se.createName("Muhahah", "evil", "stuff");
            return sb.addBodyElement(name);

        } catch (SOAPException e) {
            throw new RuntimeException("MACHandler: error trying to build the header element");
        }
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


    @Override
    public boolean handleFault(SOAPMessageContext context) {return false; }

    @Override
    public Set<QName> getHeaders() {return null; }

    @Override
    public void close(MessageContext context) {   }

}
