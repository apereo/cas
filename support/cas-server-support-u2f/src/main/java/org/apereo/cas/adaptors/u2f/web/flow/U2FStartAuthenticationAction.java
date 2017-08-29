package org.apereo.cas.adaptors.u2f.web.flow;

import com.yubico.u2f.U2F;
import com.yubico.u2f.data.messages.SignRequest;
import com.yubico.u2f.data.messages.SignRequestData;
import org.apereo.cas.adaptors.u2f.U2FAuthentication;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link U2FStartAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FStartAuthenticationAction extends AbstractAction {

    private final U2F u2f = new U2F();
    private final String serverAddress;
    private final U2FDeviceRepository u2FDeviceRepository;

    public U2FStartAuthenticationAction(final String serverAddress, final U2FDeviceRepository u2FDeviceRepository) {
        this.serverAddress = serverAddress;
        this.u2FDeviceRepository = u2FDeviceRepository;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Principal p = WebUtils.getAuthentication(requestContext).getPrincipal();
        final SignRequestData requestData = u2f.startSignature(this.serverAddress, u2FDeviceRepository.getRegisteredDevices(p.getId()));
        u2FDeviceRepository.requestDeviceAuthentication(requestData.getRequestId(), p.getId(), requestData.toJson());

        if (!requestData.getSignRequests().isEmpty()) {
            final SignRequest req = requestData.getSignRequests().get(0);
            requestContext.getFlowScope().put("u2fAuth", new U2FAuthentication(req.getChallenge(), req.getAppId(), req.getKeyHandle()));
            return success();
        }
        return error();
    }
}
