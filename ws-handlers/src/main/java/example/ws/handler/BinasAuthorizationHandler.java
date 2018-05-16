package example.ws.handler;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

public class BinasAuthorizationHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String CLIENT_NAME = "clientName";

    private static final String MAC = "mac";
    //private static final String NS2 = "ns2";
    private static final String NS2 = "ns2";
    private static final String URI = "http://ws.binas.org/";


    private String getSoapBody(SOAPMessageContext smc) throws SOAPException, TransformerException {
        SOAPBody element = smc.getMessage().getSOAPBody();
        DOMSource source = new DOMSource(element);
        StringWriter stringResult = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
        return stringResult.toString();
    }

    private String getClientNameFromRequestBody(SOAPMessageContext context) throws SOAPException {
        SOAPBody body = context.getMessage().getSOAPBody();


        Node requestedMethod = body.getFirstChild();
        if(requestedMethod != null && requestedMethod.getAttributes().getLength() > 0) {
            Node requestedMethodParameters = requestedMethod.getFirstChild();
            if(requestedMethodParameters != null
                    && requestedMethodParameters.getAttributes().getLength() > 0
                    && (requestedMethodParameters.getLocalName() == "email" || requestedMethodParameters.getLocalName() == "user")) {
                return requestedMethodParameters.getFirstChild().getNodeValue();
            }
        }
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        //System.out.println("---------------------------- KerberosServerHandler: Handling message. ----------------------------");

        Boolean outboundElement = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (!outboundElement.booleanValue()) {
                System.out.println("BinasAuthorizationHandler: Receiving inbound SOAP message...");

                String clientName = (String) context.get(CLIENT_NAME);

                String requestClientName = getClientNameFromRequestBody(context);

                if(requestClientName != null && !clientName.equals(requestClientName))
                    throw new RuntimeException("BinasAuthorizationHandler: SECURITY WARNING mismatch in client emails");
                else
                    System.out.println("BinasAuthorizationHandler: OK");

            } else {
                System.out.println("BinasAuthorizationHandler: Receiving outbound SOAP message... ignoring");

            }
        } catch (Exception e) {
            System.out.print("Caught exception in handleMessage: ");
            throw new RuntimeException("bla");
        }

        //System.out.println("-------------------------- KerberosClientHandler: END Handling message. --------------------------");

        return true;
    }


    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }
    @Override
    public void close(MessageContext context) {  }
    @Override
    public Set<QName> getHeaders() {
        return null;
    }

}
