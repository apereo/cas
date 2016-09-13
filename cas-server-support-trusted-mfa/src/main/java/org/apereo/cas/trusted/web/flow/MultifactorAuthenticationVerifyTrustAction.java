package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.trusted.authentication.AuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
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
        final Credential c = WebUtils.getCredential(requestContext);
        if (c == null) {
            LOGGER.warn("Could not determine credential from the request context");
            return no();
        }
        final LocalDate onOrAfter = LocalDate.now().minusDays(this.numberOfDays);
        final Set<AuthenticationTrustRecord> results = storage.get(c.getId(), onOrAfter);
        if (results.isEmpty()) {
            LOGGER.warn("No valid trusted authentication records could be found for {}", c.getId());
            return no();
        }
        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        final String geography = clientInfo.getClientIpAddress()
                .concat(".").concat(WebUtils.getHttpServletRequestUserAgent());
        if (!results.stream()
                .filter(entry -> entry.getGeography().equals(geography))
                .findAny()
                .isPresent()) {
            LOGGER.warn("No trusted authentication records could be found for {} to match the current geography", c.getId());
            return no();
        }

        LOGGER.debug("Trusted authentication records found for {} that matches the current geography", c.getId());
        return yes();
    }

    public void setStorage(final MultifactorAuthenticationTrustStorage storage) {
        this.storage = storage;
    }
    
    public void setNumberOfDays(final long numberOfDays) {
        this.numberOfDays = numberOfDays;
    }
}
