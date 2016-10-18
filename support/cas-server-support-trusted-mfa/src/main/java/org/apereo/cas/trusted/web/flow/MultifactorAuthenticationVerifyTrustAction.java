package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;
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
 * @since 5.0.0
 */
public class MultifactorAuthenticationVerifyTrustAction extends AbstractAction {
    

    private static final Logger LOGGER = LoggerFactory.getLogger(MultifactorAuthenticationVerifyTrustAction.class);
    
    private MultifactorAuthenticationTrustStorage storage;

    private MultifactorAuthenticationProperties.Trusted trustedProperties;
    
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Authentication c = WebUtils.getAuthentication(requestContext);
        if (c == null) {
            LOGGER.warn("Could not determine authentication from the request context");
            return no();
        }
        final String principal = c.getPrincipal().getId();
        final LocalDate onOrAfter = LocalDate.now().minus(trustedProperties.getExpiration(),
                DateTimeUtils.toChronoUnit(trustedProperties.getTimeUnit()));
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
        requestContext.getFlashScope()
                .put(AbstractMultifactorTrustedDeviceWebflowConfigurer.MFA_TRUSTED_AUTHN_SCOPE_ATTR, Boolean.TRUE);
        return yes();
    }

    public void setStorage(final MultifactorAuthenticationTrustStorage storage) {
        this.storage = storage;
    }

    public void setTrustedProperties(final MultifactorAuthenticationProperties.Trusted trustedProperties) {
        this.trustedProperties = trustedProperties;
    }
}
