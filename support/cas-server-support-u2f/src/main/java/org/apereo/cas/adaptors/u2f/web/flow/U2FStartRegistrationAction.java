package org.apereo.cas.adaptors.u2f.web.flow;

import com.yubico.u2f.U2F;
import com.yubico.u2f.data.messages.RegisterRequest;
import com.yubico.u2f.data.messages.RegisterRequestData;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.U2FRegistration;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link U2FStartRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FStartRegistrationAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(U2FStartAuthenticationAction.class);

    private final U2F u2f = new U2F();
    private final String serverAddress;
    private final U2FDeviceRepository u2FDeviceRepository;

    public U2FStartRegistrationAction(final String serverAddress, final U2FDeviceRepository u2FDeviceRepository) {
        this.serverAddress = serverAddress;
        this.u2FDeviceRepository = u2FDeviceRepository;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Principal p = WebUtils.getAuthentication(requestContext).getPrincipal();
        final RegisterRequestData registerRequestData = u2f.startRegistration(this.serverAddress, u2FDeviceRepository.getRegisteredDevices(p.getId()));
        u2FDeviceRepository.requestDeviceRegistration(registerRequestData.getRequestId(), p.getId(), registerRequestData.toJson());
        if (!registerRequestData.getRegisterRequests().isEmpty()) {
            final RegisterRequest req = registerRequestData.getRegisterRequests().get(0);
            requestContext.getFlowScope().put("u2fReg", new U2FRegistration(req.getChallenge(), req.getAppId()));
            return success();
        }
        return error();
    }
}
