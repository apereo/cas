package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.time.LocalDate;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This is {@link MultifactorAuthenticationVerifyTrustAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MultifactorAuthenticationVerifyTrustAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultifactorAuthenticationVerifyTrustAction.class);

    private final MultifactorAuthenticationTrustStorage storage;
    private final TrustedDevicesMultifactorProperties trustedProperties;

    public MultifactorAuthenticationVerifyTrustAction(final MultifactorAuthenticationTrustStorage storage,
                                                      final TrustedDevicesMultifactorProperties trustedProperties) {
        this.storage = storage;
        this.trustedProperties = trustedProperties;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Authentication c = WebUtils.getAuthentication(requestContext);
        if (c == null) {
            LOGGER.warn("Could not determine authentication from the request context");
            return no();
        }
        final String principal = c.getPrincipal().getId();
        final ChronoUnit unit = DateTimeUtils.toChronoUnit(trustedProperties.getTimeUnit());
        final LocalDate onOrAfter = LocalDateTime.now().minus(trustedProperties.getExpiration(), unit).toLocalDate();
        LOGGER.warn("Retrieving trusted authentication records for [{}] that are on/after [{}]", principal, onOrAfter);
        final Set<MultifactorAuthenticationTrustRecord> results = storage.get(principal, onOrAfter);
        if (results.isEmpty()) {
            LOGGER.debug("No valid trusted authentication records could be found for [{}]", principal);
            return no();
        }
        final String geography = MultifactorAuthenticationTrustUtils.generateGeography();
        LOGGER.debug("Retrieving authentication records for [{}] that match [{}]", principal, geography);
        if (results.stream()
                .noneMatch(entry -> entry.getGeography().equals(geography))) {
            LOGGER.debug("No trusted authentication records could be found for [{}] to match the current geography", principal);
            return no();
        }

        LOGGER.debug("Trusted authentication records found for [{}] that matches the current geography", principal);

        MultifactorAuthenticationTrustUtils.setMultifactorAuthenticationTrustedInScope(requestContext);
        MultifactorAuthenticationTrustUtils.trackTrustedMultifactorAuthenticationAttribute(
                c,
                trustedProperties.getAuthenticationContextAttribute());
        return yes();
    }
}
