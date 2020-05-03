package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.adaptors.u2f.U2FAuthentication;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.u2f.U2F;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link U2FStartAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class U2FStartAuthenticationAction extends AbstractAction {

    private final U2F u2f;

    private final String serverAddress;

    private final U2FDeviceRepository u2FDeviceRepository;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val p = WebUtils.getAuthentication(requestContext).getPrincipal();
        val requestData = u2f.startSignature(this.serverAddress, u2FDeviceRepository.getRegisteredDevices(p.getId()));
        u2FDeviceRepository.requestDeviceAuthentication(requestData.getRequestId(), p.getId(), requestData.toJson());

        if (!requestData.getSignRequests().isEmpty()) {
            val req = requestData.getSignRequests().get(0);
            val u2fAuth = new U2FAuthentication(req.getChallenge(), req.getAppId(), req.getKeyHandle());
            requestContext.getFlowScope().put("u2fAuth", u2fAuth);
            return success();
        }
        return error();
    }
}
