package org.apereo.cas.web.flow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentReminderOptions;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AbstractConsentAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractConsentAction extends AbstractAction {
    /**
     * CAS Settings.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * The services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Service selection strategies.
     */
    protected final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    /**
     * The consent engine that handles calculations.
     */
    protected final ConsentEngine consentEngine;

    /**
     * Gets registered service for consent.
     *
     * @param requestContext the request context
     * @param service        the service
     * @return the registered service for consent
     */
    protected RegisteredService getRegisteredServiceForConsent(final RequestContext requestContext, final Service service) {
        final var serviceToUse = this.authenticationRequestServiceSelectionStrategies.resolveService(service);
        final var registeredService = this.servicesManager.findServiceBy(serviceToUse);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        return registeredService;
    }

    /**
     * Prepare consent for request context.
     *
     * @param requestContext the request context
     */
    protected void prepareConsentForRequestContext(final RequestContext requestContext) {
        final var consentProperties = casProperties.getConsent();

        final var service = this.authenticationRequestServiceSelectionStrategies.resolveService(WebUtils.getService(requestContext));
        final var registeredService = getRegisteredServiceForConsent(requestContext, service);
        final var authentication = WebUtils.getAuthentication(requestContext);
        final var attributes = consentEngine.resolveConsentableAttributesFrom(authentication, service, registeredService);
        final var flowScope = requestContext.getFlowScope();
        flowScope.put("attributes", attributes);
        flowScope.put("principal", authentication.getPrincipal().getId());
        flowScope.put("service", service);

        final var decision = consentEngine.findConsentDecision(service, registeredService, authentication);
        flowScope.put("option", decision == null ? ConsentReminderOptions.ATTRIBUTE_NAME.getValue() : decision.getOptions().getValue());

        final long reminder = decision == null ? consentProperties.getReminder() : decision.getReminder();
        flowScope.put("reminder", Long.valueOf(reminder));
        flowScope.put("reminderTimeUnit", decision == null
            ? consentProperties.getReminderTimeUnit().name() : decision.getReminderTimeUnit().name());
    }
}
