package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;

/**
 * This is {@link CheckConsentRequiredAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CheckConsentRequiredAction extends AbstractAction {
    /**
     * Indicates that webflow should proceed with consent.
     */
    public static final String EVENT_ID_CONSENT_REQUIRED = "consentRequired";

    private final ServicesManager servicesManager;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ConsentRepository consentRepository;
    private final ConsentEngine consentEngine;

    public CheckConsentRequiredAction(final ServicesManager servicesManager,
                                      final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                      final AuthenticationSystemSupport authenticationSystemSupport,
                                      final ConsentRepository consentRepository,
                                      final ConsentEngine consentEngine) {
        this.servicesManager = servicesManager;
        this.authenticationRequestServiceSelectionStrategies = authenticationRequestServiceSelectionStrategies;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.consentRepository = consentRepository;
        this.consentEngine = consentEngine;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final String consentEvent = determineConsentEvent(requestContext);
        if (StringUtils.isBlank(consentEvent)) {
            return null;
        }
        prepareConsentForRequestContext(requestContext);
        return new EventFactorySupport().event(this, consentEvent);
    }

    private void prepareConsentForRequestContext(final RequestContext requestContext) {
        final Service service = this.authenticationRequestServiceSelectionStrategies.resolveService(WebUtils.getService(requestContext));
        final RegisteredService registeredService = getRegisteredServiceForConsent(requestContext, service);
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final Map<String, Object> attributes =
                registeredService.getAttributeReleasePolicy().getAttributes(authentication.getPrincipal(), service, registeredService);
        requestContext.getFlowScope().put("attributes", attributes);
        requestContext.getFlowScope().put("principal", authentication.getPrincipal().getId());
    }

    /**
     * Determine consent event string.
     *
     * @param requestContext the request context
     * @return the string
     */
    protected String determineConsentEvent(final RequestContext requestContext) {
        final Service service = this.authenticationRequestServiceSelectionStrategies.resolveService(WebUtils.getService(requestContext));
        if (service == null) {
            return null;
        }

        final RegisteredService registeredService = getRegisteredServiceForConsent(requestContext, service);

        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            return null;
        }

        return isConsentRequired(service, registeredService, authentication, requestContext);
    }

    private RegisteredService getRegisteredServiceForConsent(final RequestContext requestContext, final Service service) {
        RegisteredService registeredService = WebUtils.getRegisteredService(requestContext);
        if (registeredService == null) {
            registeredService = this.servicesManager.findServiceBy(service);
        }
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        return registeredService;
    }

    /**
     * Is consent required ?
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @param requestContext    the request context
     * @return the event id.
     */
    protected String isConsentRequired(final Service service, final RegisteredService registeredService,
                                       final Authentication authentication, final RequestContext requestContext) {
        final boolean required = this.consentEngine.isConsentRequiredFor(service, registeredService, authentication);
        return required ? EVENT_ID_CONSENT_REQUIRED : null;
    }
}
