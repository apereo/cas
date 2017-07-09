package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentDecision;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link AbstractConsentAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class AbstractConsentAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConsentAction.class);

    /* CAS Settings. */
    protected final CasConfigurationProperties casProperties;

    /* The services manager. */
    protected final ServicesManager servicesManager;

    /* Service selection strategies. */
    protected final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    /* The consent engine that handles calculations. */
    protected final ConsentEngine consentEngine;

    public AbstractConsentAction(final CasConfigurationProperties casProperties, final ServicesManager servicesManager,
                                 final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                 final ConsentEngine consentEngine) {
        this.casProperties = casProperties;
        this.servicesManager = servicesManager;
        this.authenticationRequestServiceSelectionStrategies = authenticationRequestServiceSelectionStrategies;
        this.consentEngine = consentEngine;
    }

    /**
     * Gets registered service for consent.
     *
     * @param requestContext the request context
     * @param service        the service
     * @return the registered service for consent
     */
    protected RegisteredService getRegisteredServiceForConsent(final RequestContext requestContext, final Service service) {
        RegisteredService registeredService = WebUtils.getRegisteredService(requestContext);
        if (registeredService == null) {
            registeredService = this.servicesManager.findServiceBy(service);
        }
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        return registeredService;
    }

    /**
     * Prepare consent for request context.
     *
     * @param requestContext the request context
     */
    protected void prepareConsentForRequestContext(final RequestContext requestContext) {
        final Service service = this.authenticationRequestServiceSelectionStrategies.resolveService(WebUtils.getService(requestContext));
        final RegisteredService registeredService = getRegisteredServiceForConsent(requestContext, service);
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final Map<String, Object> attributes = consentEngine.getConsentableAttributes(authentication, service, registeredService);
        requestContext.getFlowScope().put("attributes", attributes);
        requestContext.getFlowScope().put("principal", authentication.getPrincipal().getId());

        final ConsentDecision decision = consentEngine.findConsentDecision(service, registeredService, authentication);
        requestContext.getFlowScope().put("reminder", decision == null ? casProperties.getConsent().getReminder() : decision.getReminder());
        requestContext.getFlowScope().put("reminderTimeUnit", decision == null ?
                casProperties.getConsent().getReminderTimeUnit().name():
                decision.getReminderTimeUnit().name());
    }
}
