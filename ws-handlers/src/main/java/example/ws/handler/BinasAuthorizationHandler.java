package example.ws.handler;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

public class BinasAuthorizationHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String CLIENT_NAME = "clientName";


    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        System.out.println("---------------------------- BinasAuthorizationHandler: Handling message. ----------------------------");

        Boolean outboundElement = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (!outboundElement.booleanValue()) {
            System.out.println("BinasAuthorizationHandler: Receiving inbound SOAP message...");

            String clientName = (String) context.get(CLIENT_NAME);

            String requestClientName = getClientNameFromRequestBody(context);
            System.out.println("BinasAuthorizationHandler: validating the user email and request email");
            if(requestClientName != null && !clientName.equals(requestClientName))
                handleBinasAuthorizationError("BinasAuthorizationHandler: SECURITY WARNING mismatch in client emails",
                        "Binas: error trying to authorize your request");
            else
                System.out.println("BinasAuthorizationHandler: OK");

        } else {
            System.out.println("BinasAuthorizationHandler: Receiving outbound SOAP message... ignoring");

        }

        System.out.println("-------------------------- BinasAuthorizationHandler: END Handling message. --------------------------");

        return true;
    }

    private void handleBinasAuthorizationError(String serverErrorMessage, String clientErrorMessage) {
        System.out.println(serverErrorMessage);
        throw new RuntimeException(clientErrorMessage);
    }

    //extracts the client email from the SOAP message context
    private String getClientNameFromRequestBody(SOAPMessageContext context)  {
        try {
            SOAPBody body = context.getMessage().getSOAPBody();
            Node requestedMethod = body.getFirstChild();

            if(requestedMethod != null && requestedMethod.getAttributes().getLength() > 0) {
                Node requestedMethodParameters = requestedMethod.getFirstChild();
                if(requestedMethodParameters != null
                        && requestedMethodParameters.getAttributes().getLength() > 0
                        && (requestedMethodParameters.getLocalName() == "email")) {
                    return requestedMethodParameters.getFirstChild().getNodeValue();
                }
            }
        } catch (SOAPException e) {
            handleBinasAuthorizationError("BinasAuthorizationHandler: error trying to get the client name from the soap message body",
                    "Binas: error trying to handle your request");
        }
        return null;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {return false; }
    @Override
    public void close(MessageContext context) {  }
    @Override
    public Set<QName> getHeaders() { return null; }

}
