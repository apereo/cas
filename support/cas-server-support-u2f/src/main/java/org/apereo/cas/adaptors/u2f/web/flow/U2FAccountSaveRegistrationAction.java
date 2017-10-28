package org.apereo.cas.adaptors.u2f.web.flow;

import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.RegisterResponse;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link U2FAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FAccountSaveRegistrationAction extends AbstractAction {
    private final U2F u2f = new U2F();
    private final U2FDeviceRepository u2FDeviceRepository;

    public U2FAccountSaveRegistrationAction(final U2FDeviceRepository u2FDeviceRepository) {
        this.u2FDeviceRepository = u2FDeviceRepository;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Principal p = WebUtils.getAuthentication(requestContext).getPrincipal();
        final String response = requestContext.getRequestParameters().get("tokenResponse");
        final RegisterResponse registerResponse = RegisterResponse.fromJson(response);
        final String regReqJson = u2FDeviceRepository.getDeviceRegistrationRequest(registerResponse.getRequestId(), p.getId());
        final RegisterRequestData registerRequestData = RegisterRequestData.fromJson(regReqJson);
        final DeviceRegistration registration = u2f.finishRegistration(registerRequestData, registerResponse);
        u2FDeviceRepository.registerDevice(p.getId(), registration);
        return success();
    }
}
