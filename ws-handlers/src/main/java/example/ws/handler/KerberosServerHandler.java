package example.ws.handler;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

public class KerberosServerHandler implements SOAPHandler<SOAPMessageContext> {
    @Override
    public Set<QName> getHeaders() {
        return null;//TODO
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        return false;//TODO
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;//TODO
    }

    @Override
    public void close(MessageContext context) {
        //TODO
    }
}