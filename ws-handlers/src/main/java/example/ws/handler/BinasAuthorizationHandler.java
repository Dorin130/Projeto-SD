package example.ws.handler;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BinasAuthorizationHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String CLIENT_NAME = "clientName";
    private static final Set<String> AUTH_REQ = new HashSet<>(Arrays.asList("activateUser", "returnBina", "rentBina", "getCredit"));


    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        System.out.println("---------------------------- BinasAuthorizationHandler: Handling message. ----------------------------");

        Boolean outboundElement = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (!outboundElement.booleanValue()) {
            //System.out.println("BinasAuthorizationHandler: Receiving inbound SOAP message...");

            String clientName = (String) context.get(CLIENT_NAME);
            try {
                if (!AUTH_REQ.contains(context.getMessage().getSOAPBody().getFirstChild().getLocalName())) {
                    return true;
                }
            } catch (SOAPException e) {
                handleBinasAuthorizationError("BinasAuthorizationHandler: error trying to get the client name from the soap message body",
                        "Binas: error trying to handle your request");
            }


            String requestClientName = getClientNameFromRequestBody(context);
            //System.out.println("BinasAuthorizationHandler: validating the user email and request email");
            if(requestClientName != null && !clientName.equals(requestClientName))
                handleBinasAuthorizationError("BinasAuthorizationHandler: SECURITY WARNING mismatch in client emails",
                        "Binas: error trying to authorize your request");
            //else
            //    System.out.println("BinasAuthorizationHandler: OK");

        } else {
            //System.out.println("BinasAuthorizationHandler: Receiving outbound SOAP message... ignoring");

        }

        //System.out.println("-------------------------- BinasAuthorizationHandler: END Handling message. --------------------------");

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
            return body.getElementsByTagName("email").item(0).getTextContent();

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
