package org.apereo.cas.trusted.web.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * This is {@link MultifactorAuthenticationVerifyTrustAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class MultifactorAuthenticationVerifyTrustAction extends AbstractAction {

    private final MultifactorAuthenticationTrustStorage storage;
    private final DeviceFingerprintStrategy deviceFingerprintStrategy;
    private final TrustedDevicesMultifactorProperties trustedProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Authentication c = WebUtils.getAuthentication(requestContext);
        if (c == null) {
            LOGGER.warn("Could not determine authentication from the request context");
            return no();
        }
        final String principal = c.getPrincipal().getId();
        final ChronoUnit unit = DateTimeUtils.toChronoUnit(trustedProperties.getTimeUnit());
        final LocalDateTime onOrAfter = LocalDateTime.now().minus(trustedProperties.getExpiration(), unit);
        LOGGER.debug("Retrieving trusted authentication records for [{}] that are on/after [{}]", principal, onOrAfter);
        final Set<MultifactorAuthenticationTrustRecord> results = storage.get(principal, onOrAfter);
        if (results.isEmpty()) {
            LOGGER.debug("No valid trusted authentication records could be found for [{}]", principal);
            return no();
        }
        final String fingerprint = deviceFingerprintStrategy.determineFingerprint(principal, requestContext, false);
        LOGGER.debug("Retrieving authentication records for [{}] that matches [{}]", principal, fingerprint);
        if (results.stream().noneMatch(entry -> entry.getDeviceFingerprint().equals(fingerprint))) {
            LOGGER.debug("No trusted authentication records could be found for [{}] to match the current device fingerprint", principal);
            return no();
        }

        LOGGER.debug("Trusted authentication records found for [{}] that matches the current device fingerprint", principal);

        MultifactorAuthenticationTrustUtils.setMultifactorAuthenticationTrustedInScope(requestContext);
        MultifactorAuthenticationTrustUtils.trackTrustedMultifactorAuthenticationAttribute(
            c,
            trustedProperties.getAuthenticationContextAttribute());
        return yes();
    }
}
