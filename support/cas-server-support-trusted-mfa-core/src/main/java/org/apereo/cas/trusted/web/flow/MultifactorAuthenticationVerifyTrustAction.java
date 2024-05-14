package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Set;

/**
 * This is {@link MultifactorAuthenticationVerifyTrustAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class MultifactorAuthenticationVerifyTrustAction extends BaseCasWebflowAction {

    private final MultifactorAuthenticationTrustStorage storage;

    private final DeviceFingerprintStrategy deviceFingerprintStrategy;

    private final TrustedDevicesMultifactorProperties trustedProperties;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final MultifactorAuthenticationTrustedDeviceBypassEvaluator bypassEvaluator;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            LOGGER.warn("Could not determine authentication from the request context");
            return no();
        }
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val service = WebUtils.getService(requestContext);

        val trustedDevicesDisabled = MultifactorAuthenticationTrustUtils.isMultifactorAuthenticationTrustedDevicesDisabled(requestContext);
        val publicWorkstation = WebUtils.isAuthenticatingAtPublicWorkstation(requestContext);
        if (publicWorkstation || trustedDevicesDisabled || bypassEvaluator.shouldBypassTrustedDevice(registeredService, service, authentication)) {
            LOGGER.debug("Trusted device registration is disabled for [{}]", registeredService);
            return result(CasWebflowConstants.TRANSITION_ID_SKIP);
        }
        val principal = authentication.getPrincipal().getId();
        LOGGER.trace("Retrieving trusted authentication records for [{}]", principal);
        val results = storage.isAvailable() ? storage.get(principal) : Set.<MultifactorAuthenticationTrustRecord>of();
        if (results.isEmpty()) {
            LOGGER.debug("No valid trusted authentication records could be found for [{}]", principal);
            return no();
        }
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val fingerprint = deviceFingerprintStrategy.determineFingerprint(authentication, request, response);
        LOGGER.trace("Retrieving authentication records for [{}] that matches [{}]", principal, fingerprint);
        val foundRecord = results.stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getDeviceFingerprint()))
            .filter(entry -> entry.getDeviceFingerprint().equals(fingerprint))
            .findAny();
        if (foundRecord.isEmpty()) {
            LOGGER.debug("No trusted authentication records could be found for [{}] to match the current device fingerprint", principal);
            return no();
        }
        LOGGER.debug("Trusted authentication records found for [{}] that matches the current device fingerprint", principal);
        MultifactorAuthenticationTrustUtils.setMultifactorAuthenticationTrustedInScope(requestContext);
        MultifactorAuthenticationTrustUtils.trackTrustedMultifactorAuthenticationAttribute(
            authentication, trustedProperties.getCore().getAuthenticationContextAttribute());
        MultifactorAuthenticationTrustUtils.putMultifactorAuthenticationTrustRecord(requestContext, foundRecord.get());
        return yes();
    }
}
