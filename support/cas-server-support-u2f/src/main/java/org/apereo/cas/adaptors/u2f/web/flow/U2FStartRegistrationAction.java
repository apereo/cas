package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.adaptors.u2f.U2FRegistration;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link U2FStartRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class U2FStartRegistrationAction extends AbstractAction {

    private final U2F u2f;

    private final String serverAddress;

    private final U2FDeviceRepository u2FDeviceRepository;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val p = WebUtils.getAuthentication(requestContext).getPrincipal();
        val registeredDevices = u2FDeviceRepository.getRegisteredDevices(p.getId())
            .stream()
            .map(u2FDeviceRepository::decode)
            .map(Unchecked.function(r -> DeviceRegistration.fromJson(r.getRecord())))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        val registerRequestData = u2f.startRegistration(this.serverAddress, registeredDevices);
        u2FDeviceRepository.requestDeviceRegistration(registerRequestData.getRequestId(), p.getId(), registerRequestData.toJson());
        if (!registerRequestData.getRegisterRequests().isEmpty()) {
            val req = registerRequestData.getRegisterRequests().get(0);
            val u2fReg = new U2FRegistration(req.getChallenge(), req.getAppId(),
                registerRequestData.getRequestId(), p.getId(), registerRequestData.toJson());
            requestContext.getFlowScope().put("u2fReg", u2fReg);
            return success();
        }
        return error();
    }
}
