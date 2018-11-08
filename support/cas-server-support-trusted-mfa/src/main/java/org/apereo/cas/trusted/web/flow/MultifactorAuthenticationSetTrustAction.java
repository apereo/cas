package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorAuthenticationSetTrustAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class MultifactorAuthenticationSetTrustAction extends AbstractAction {
    private static final String PARAM_NAME_DEVICE_NAME = "deviceName";

    private final MultifactorAuthenticationTrustStorage storage;
    private final DeviceFingerprintStrategy deviceFingerprintStrategy;
    private final TrustedDevicesMultifactorProperties trustedProperties;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val c = WebUtils.getAuthentication(requestContext);
        if (c == null) {
            LOGGER.error("Could not determine authentication from the request context");
            return error();
        }

        AuthenticationCredentialsThreadLocalBinder.bindCurrent(c);

        val principal = c.getPrincipal().getId();
        if (!MultifactorAuthenticationTrustUtils.isMultifactorAuthenticationTrustedInScope(requestContext)) {
            LOGGER.debug("Attempt to store trusted authentication record for [{}]", principal);
            val record = MultifactorAuthenticationTrustRecord.newInstance(principal,
                MultifactorAuthenticationTrustUtils.generateGeography(),
                deviceFingerprintStrategy.determineFingerprint(principal, requestContext, true));

            if (requestContext.getRequestParameters().contains(PARAM_NAME_DEVICE_NAME)) {
                val deviceName = requestContext.getRequestParameters().get(PARAM_NAME_DEVICE_NAME);
                if (StringUtils.isNotBlank(deviceName)) {
                    record.setName(deviceName);
                }
            }
            storage.set(record);
            LOGGER.debug("Saved trusted authentication record for [{}] under [{}]", principal, record.getName());
        }
        LOGGER.debug("Trusted authentication session exists for [{}]", principal);
        MultifactorAuthenticationTrustUtils.trackTrustedMultifactorAuthenticationAttribute(
            c,
            trustedProperties.getAuthenticationContextAttribute());
        return success();
    }
}
