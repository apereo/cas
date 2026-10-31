package org.apereo.cas.qr.web.flow;

import module java.base;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepository;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link QRAuthenticationAccountProfileDevicesAction}.
 * Lists in the account profile the devices authorized to sign in the user by scanning a QR code.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class QRAuthenticationAccountProfileDevicesAction extends BaseCasWebflowAction {
    /**
     * Flow scope attribute for the authorized devices.
     */
    public static final String FLOW_SCOPE_ATTR_DEVICES = "qrAuthenticationDevices";

    private final QRAuthenticationDeviceRepository repository;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        val devices = repository.getAuthorizedDevicesFor(principal.getId());
        if (!devices.isEmpty()) {
            requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_DEVICES, devices.stream().sorted().toList());
        }
        return null;
    }
}
