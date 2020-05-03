package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.u2f.U2F;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.RegisterResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link U2FAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class U2FAccountSaveRegistrationAction extends AbstractAction {
    private final U2F u2f;

    private final U2FDeviceRepository u2FDeviceRepository;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val p = WebUtils.getAuthentication(requestContext).getPrincipal();
        val response = requestContext.getRequestParameters().get("tokenResponse");
        val registerResponse = RegisterResponse.fromJson(response);
        val regReqJson = u2FDeviceRepository.getDeviceRegistrationRequest(registerResponse.getRequestId(), p.getId());
        val registerRequestData = RegisterRequestData.fromJson(regReqJson);
        val registration = u2f.finishRegistration(registerRequestData, registerResponse);
        u2FDeviceRepository.registerDevice(p.getId(), registration);
        return success();
    }
}
