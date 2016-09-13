package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.trusted.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.time.LocalDate;
import java.util.Set;

/**
 * This is {@link MultifactorAuthenticationVerifyTrustAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MultifactorAuthenticationVerifyTrustAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultifactorAuthenticationVerifyTrustAction.class);

    private MultifactorAuthenticationTrustStorage storage;

    private long numberOfDays;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Authentication c = WebUtils.getAuthentication(requestContext);
        if (c == null) {
            LOGGER.warn("Could not determine authentication from the request context");
            return no();
        }
        final String principal = c.getPrincipal().getId();
        final LocalDate onOrAfter = LocalDate.now().minusDays(this.numberOfDays);
        LOGGER.warn("Retrieving trusted authentication records for {} that are on/after {}", principal, onOrAfter);
        final Set<MultifactorAuthenticationTrustRecord> results = storage.get(principal, onOrAfter);
        if (results.isEmpty()) {
            LOGGER.debug("No valid trusted authentication records could be found for {}", principal);
            return no();
        }
        final String geography = MultifactorAuthenticationTrustUtils.generateGeography();
        LOGGER.debug("Retrieving authentication records for {} that match {}", principal, geography);
        if (!results.stream()
                .filter(entry -> entry.getGeography().equals(geography))
                .findAny()
                .isPresent()) {
            LOGGER.debug("No trusted authentication records could be found for {} to match the current geography", principal);
            return no();
        }

        LOGGER.debug("Trusted authentication records found for {} that matches the current geography", principal);
        return yes();
    }

    public void setStorage(final MultifactorAuthenticationTrustStorage storage) {
        this.storage = storage;
    }

    public void setNumberOfDays(final long numberOfDays) {
        this.numberOfDays = numberOfDays;
    }
}
