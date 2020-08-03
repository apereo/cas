package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorAuthenticationVerifyTrustAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class MultifactorAuthenticationVerifyTrustAction extends AbstractAction {

    private final MultifactorAuthenticationTrustStorage storage;

    private final DeviceFingerprintStrategy deviceFingerprintStrategy;

    private final TrustedDevicesMultifactorProperties trustedProperties;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final MultifactorAuthenticationTrustedDeviceBypassEvaluator bypassEvaluator;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authn = WebUtils.getAuthentication(requestContext);
        if (authn == null) {
            LOGGER.warn("Could not determine authentication from the request context");
            return no();
        }
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val service = WebUtils.getService(requestContext);
        if (bypassEvaluator.shouldBypassTrustedDevice(registeredService, service, authn)) {
            LOGGER.debug("Trusted device registration is disabled for [{}]", registeredService);
            return result(CasWebflowConstants.TRANSITION_ID_SKIP);
        }
        val principal = authn.getPrincipal().getId();
        LOGGER.trace("Retrieving trusted authentication records for [{}]", principal);
        val results = storage.get(principal);
        if (results.isEmpty()) {
            LOGGER.debug("No valid trusted authentication records could be found for [{}]", principal);
            return no();
        }
        val fingerprint = deviceFingerprintStrategy.determineFingerprint(principal, requestContext, false);
        LOGGER.trace("Retrieving authentication records for [{}] that matches [{}]", principal, fingerprint);
        if (results.stream().noneMatch(entry -> entry.getDeviceFingerprint().equals(fingerprint))) {
            LOGGER.debug("No trusted authentication records could be found for [{}] to match the current device fingerprint", principal);
            return no();
        }

        LOGGER.debug("Trusted authentication records found for [{}] that matches the current device fingerprint", principal);
        MultifactorAuthenticationTrustUtils.setMultifactorAuthenticationTrustedInScope(requestContext);
        MultifactorAuthenticationTrustUtils.trackTrustedMultifactorAuthenticationAttribute(
            authn,
            trustedProperties.getAuthenticationContextAttribute());
        return yes();
    }
}
