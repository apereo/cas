package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CheckConsentRequiredAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CheckConsentRequiredAction extends AbstractConsentAction {
    /**
     * Indicates that webflow should proceed with consent.
     */
    public static final String EVENT_ID_CONSENT_REQUIRED = "consentRequired";

    public CheckConsentRequiredAction(final ServicesManager servicesManager,
                                      final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                      final ConsentEngine consentEngine,
                                      final CasConfigurationProperties casProperties) {
        super(casProperties, servicesManager, authenticationRequestServiceSelectionStrategies, consentEngine);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final String consentEvent = determineConsentEvent(requestContext);
        if (StringUtils.isBlank(consentEvent)) {
            return null;
        }
        prepareConsentForRequestContext(requestContext);
        return new EventFactorySupport().event(this, consentEvent);
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
                                       final Authentication authentication,
                                       final RequestContext requestContext) {
        final boolean required = this.consentEngine.isConsentRequiredFor(service, registeredService, authentication).getKey();
        return required ? EVENT_ID_CONSENT_REQUIRED : null;
    }
}
