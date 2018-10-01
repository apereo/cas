package org.apereo.cas.web.flow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.consent.ConsentProperties;
import org.apereo.cas.consent.ConsentDecision;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentReminderOptions;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;

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
        final Service serviceToUse = this.authenticationRequestServiceSelectionStrategies.resolveService(service);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(serviceToUse);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        return registeredService;
    }

    /**
     * Prepare consent for request context.
     *
     * @param requestContext the request context
     */
    protected void prepareConsentForRequestContext(final RequestContext requestContext) {
        final ConsentProperties consentProperties = casProperties.getConsent();

        final Service service = this.authenticationRequestServiceSelectionStrategies.resolveService(WebUtils.getService(requestContext));
        final RegisteredService registeredService = getRegisteredServiceForConsent(requestContext, service);
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final Map<String, Object> attributes = consentEngine.resolveConsentableAttributesFrom(authentication, service, registeredService);
        final MutableAttributeMap<Object> flowScope = requestContext.getFlowScope();
        flowScope.put("attributes", attributes);
        flowScope.put("principal", authentication.getPrincipal());
        flowScope.put("service", service);

        final ConsentDecision decision = consentEngine.findConsentDecision(service, registeredService, authentication);
        flowScope.put("option", decision == null ? ConsentReminderOptions.ATTRIBUTE_NAME.getValue() : decision.getOptions().getValue());

        final long reminder = decision == null ? consentProperties.getReminder() : decision.getReminder();
        flowScope.put("reminder", Long.valueOf(reminder));
        flowScope.put("reminderTimeUnit", decision == null
            ? consentProperties.getReminderTimeUnit().name() : decision.getReminderTimeUnit().name());
    }
}
