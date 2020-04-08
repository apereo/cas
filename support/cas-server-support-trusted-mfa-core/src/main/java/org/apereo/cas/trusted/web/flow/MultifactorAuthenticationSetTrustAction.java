package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
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

import java.time.temporal.ChronoUnit;

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

    private final MultifactorAuthenticationTrustStorage storageService;

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
        val deviceBean = WebUtils.getMultifactorAuthenticationTrustRecord(requestContext, MultifactorAuthenticationTrustBean.class);
        if (deviceBean.isEmpty()) {
            LOGGER.debug("No device information is provided. Trusted authentication record is not stored and tracked");
            return success();
        }
        val deviceRecord = deviceBean.get();
        if (StringUtils.isBlank(deviceRecord.getDeviceName())) {
            LOGGER.debug("No device name is provided. Trusted authentication record is not stored and tracked");
            return success();
        }

        if (!MultifactorAuthenticationTrustUtils.isMultifactorAuthenticationTrustedInScope(requestContext)) {
            storeTrustedAuthenticationRecord(requestContext, authn, deviceRecord);
        }
        LOGGER.debug("Trusted authentication session exists for [{}]", authn.getPrincipal().getId());
        MultifactorAuthenticationTrustUtils.trackTrustedMultifactorAuthenticationAttribute(
            authn,
            trustedProperties.getAuthenticationContextAttribute());
        WebUtils.putAuthentication(authn, requestContext);
        return success();
    }

    /**
     * Store trusted authentication record.
     *
     * @param requestContext the request context
     * @param authentication the authentication
     * @param deviceRecord   the device record
     */
    protected void storeTrustedAuthenticationRecord(final RequestContext requestContext,
                                                    final Authentication authentication,
                                                    final MultifactorAuthenticationTrustBean deviceRecord) {
        val principal = authentication.getPrincipal().getId();
        LOGGER.debug("Attempting to store trusted authentication record for [{}] as device [{}]", principal, deviceRecord.getDeviceName());
        val fingerprint = deviceFingerprintStrategy.determineFingerprint(principal, requestContext, true);
        val record = MultifactorAuthenticationTrustRecord.newInstance(principal,
            MultifactorAuthenticationTrustUtils.generateGeography(), fingerprint);
        record.setName(deviceRecord.getDeviceName());
        if (deviceRecord.getTimeUnit() != ChronoUnit.FOREVER && deviceRecord.getExpiration() > 0) {
            record.expireIn(deviceRecord.getExpiration(), deviceRecord.getTimeUnit());
        } else {
            record.neverExpire();
        }
        LOGGER.debug("Trusted authentication record will expire at [{}]", record.getExpirationDate());
        this.storageService.save(record);
        LOGGER.debug("Saved trusted authentication record for [{}] under [{}]", principal, record.getName());
    }
}
