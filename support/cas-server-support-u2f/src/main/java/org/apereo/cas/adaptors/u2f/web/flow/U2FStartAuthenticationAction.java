package org.apereo.cas.adaptors.u2f.web.flow;

import com.yubico.u2f.U2F;
import com.yubico.u2f.data.messages.AuthenticateRequestData;
import org.apereo.cas.adaptors.u2f.U2FAuthentication;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;

/**
 * This is {@link U2FStartAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FStartAuthenticationAction extends AbstractAction {
    private final U2F u2f = new U2F();
    private final String serverAddress;

    public U2FStartAuthenticationAction(final String serverAddress) {
        this.serverAddress = serverAddress;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final AuthenticateRequestData requestData = u2f.startAuthentication(this.serverAddress, new ArrayList<>());
        requestContext.getFlowScope().put("u2fAuth", new U2FAuthentication("casuser", requestData.toJson()));
        return success();
    }
}
