package org.apereo.cas.adaptors.u2f.web.flow;

import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.RegisterResponse;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistrationRepository;
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
    private final U2FDeviceRegistrationRepository u2FDeviceRegistrationRepository;

    public U2FAccountSaveRegistrationAction(final U2FDeviceRegistrationRepository u2FDeviceRegistrationRepository) {
        this.u2FDeviceRegistrationRepository = u2FDeviceRegistrationRepository;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Principal p = WebUtils.getAuthentication(requestContext).getPrincipal();
        final String response = requestContext.getRequestParameters().get("response");
        final RegisterResponse registerResponse = RegisterResponse.fromJson(response);
        final String regReqJson = u2FDeviceRegistrationRepository.getRequestStorage().remove(registerResponse.getRequestId());
        final RegisterRequestData registerRequestData = RegisterRequestData.fromJson(regReqJson);
        final DeviceRegistration registration = u2f.finishRegistration(registerRequestData, registerResponse);
        u2FDeviceRegistrationRepository.addRegistration(p.getId(), registration);
        return success();
    }
}
