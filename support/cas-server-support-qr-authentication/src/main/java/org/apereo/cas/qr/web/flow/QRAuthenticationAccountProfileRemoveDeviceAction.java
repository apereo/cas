package org.apereo.cas.qr.web.flow;

import module java.base;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepository;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link QRAuthenticationAccountProfileRemoveDeviceAction}.
 * Removes a QR code login device, only when it is authorized for the authenticated principal.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class QRAuthenticationAccountProfileRemoveDeviceAction extends BaseCasWebflowAction {
    private final QRAuthenticationDeviceRepository repository;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val deviceId = requestContext.getRequestParameters().getRequired("deviceId");
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        if (!repository.getAuthorizedDevicesFor(principal.getId()).contains(deviceId)) {
            LOGGER.warn("Device [{}] is not authorized for [{}] and cannot be removed", deviceId, principal.getId());
            return error();
        }
        repository.removeDevice(deviceId);
        WebUtils.addInfoMessageToContext(requestContext, "screen.account.qrdevices.removed");
        return success();
    }
}
