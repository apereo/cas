package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationTrustedDeviceProviderAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultMultifactorAuthenticationTrustedDeviceProviderAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class DefaultMultifactorAuthenticationTrustedDeviceProviderAction extends BaseCasWebflowAction implements MultifactorAuthenticationTrustedDeviceProviderAction {
    protected final MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();
        val devices = mfaTrustEngine.get(principal.getId())
            .stream()
            .map(device -> mapMultifactorAuthenticationTrustRecord(requestContext, device))
            .toList();
        MultifactorAuthenticationTrustUtils.putMultifactorAuthenticationTrustedDevices(requestContext, devices);
        return null;
    }

    protected MultifactorAuthenticationRegisteredDevice mapMultifactorAuthenticationTrustRecord(
        final RequestContext requestContext, final MultifactorAuthenticationTrustRecord device) {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val source = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(device.getMultifactorAuthenticationProvider(), applicationContext)
            .map(MultifactorAuthenticationProvider::getFriendlyName)
            .orElse("Unknown");
        val expirationDate = DateTimeUtils.zonedDateTimeOf(device.getExpirationDate());
        return MultifactorAuthenticationRegisteredDevice.builder()
            .id(String.valueOf(device.getId()))
            .model(device.getRecordKey())
            .type(device.getDeviceFingerprint())
            .name(device.getName())
            .payload(device.toJson())
            .expirationDateTime(expirationDate != null ? expirationDate.toString() : null)
            .lastUsedDateTime(device.getRecordDate().toString())
            .source(source)
            .build();
    }
}
