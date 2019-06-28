package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
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
@Getter
public class MultifactorAuthenticationSetTrustAction extends AbstractAction {
    private static final String PARAM_NAME_DEVICE_NAME = "deviceName";

    private final MultifactorAuthenticationTrustStorage storage;
    private final DeviceFingerprintStrategy deviceFingerprintStrategy;
    private final TrustedDevicesMultifactorProperties trustedProperties;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    private final MultifactorAuthenticationTrustedDeviceBypassEvaluator bypassEvaluator;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val authn = WebUtils.getAuthentication(requestContext);
        if (authn == null) {
            LOGGER.error("Could not determine authentication from the request context");
            return error();
        }

        val registeredService = WebUtils.getRegisteredService(requestContext);
        val service = WebUtils.getService(requestContext);
        
        if (bypassEvaluator.shouldBypassTrustedDevice(registeredService, service, authn)) {
            LOGGER.debug("Trusted device registration is disabled for [{}]", registeredService);
            return success();
        }

        AuthenticationCredentialsThreadLocalBinder.bindCurrent(authn);
        val principal = authn.getPrincipal().getId();
        val deviceName = requestContext.getRequestParameters().get(PARAM_NAME_DEVICE_NAME, StringUtils.EMPTY);
        val providedDeviceName = StringUtils.isNotBlank(deviceName);
        if (providedDeviceName) {
            if (!MultifactorAuthenticationTrustUtils.isMultifactorAuthenticationTrustedInScope(requestContext)) {
                LOGGER.debug("Attempt to store trusted authentication record for [{}] as device [{}]", principal, deviceName);
                val fingerprint = deviceFingerprintStrategy.determineFingerprint(principal, requestContext, true);
                val record = MultifactorAuthenticationTrustRecord.newInstance(principal,
                    MultifactorAuthenticationTrustUtils.generateGeography(),
                    fingerprint);
                record.setName(deviceName);
                storage.set(record);
                LOGGER.debug("Saved trusted authentication record for [{}] under [{}]", principal, record.getName());
            }
            LOGGER.debug("Trusted authentication session exists for [{}]", principal);
            MultifactorAuthenticationTrustUtils.trackTrustedMultifactorAuthenticationAttribute(
                authn,
                trustedProperties.getAuthenticationContextAttribute());
        } else {
            LOGGER.debug("No device name is provided. Trusted authentication record is not stored and tracked for the session");
        }
        return success();
    }
}
