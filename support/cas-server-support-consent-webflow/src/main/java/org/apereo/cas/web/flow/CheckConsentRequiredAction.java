package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CheckConsentRequiredAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class CheckConsentRequiredAction extends AbstractConsentAction {
    /**
     * Indicates that webflow should proceed with consent.
     */
    public static final String EVENT_ID_CONSENT_REQUIRED = "consentRequired";

    public CheckConsentRequiredAction(final ServicesManager servicesManager,
                                      final AuthenticationServiceSelectionPlan strategies,
                                      final ConsentEngine consentEngine,
                                      final CasConfigurationProperties casProperties,
                                      final AttributeDefinitionStore attributeDefinitionStore,
                                      final ConfigurableApplicationContext applicationContext) {
        super(casProperties, servicesManager, strategies,
            consentEngine, attributeDefinitionStore, applicationContext);
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val consentEvent = determineConsentEvent(requestContext);
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
        val webService = WebUtils.getService(requestContext);
        val service = this.authenticationRequestServiceSelectionStrategies.resolveService(webService);
        if (service == null) {
            return null;
        }

        val registeredService = getRegisteredServiceForConsent(requestContext, service);
        val authentication = WebUtils.getAuthentication(requestContext);
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
    protected String isConsentRequired(final Service service,
                                       final RegisteredService registeredService,
                                       final Authentication authentication,
                                       final RequestContext requestContext) {
        val consentPolicy = registeredService.getAttributeReleasePolicy().getConsentPolicy();
        if (consentPolicy != null) {
            switch (consentPolicy.getStatus()) {
                case TRUE:
                    LOGGER.trace("Attribute consent is enabled for registered service [{}]", registeredService.getName());
                    val required = this.consentEngine.isConsentRequiredFor(service, registeredService, authentication).isRequired();
                    return required ? EVENT_ID_CONSENT_REQUIRED : null;
                case FALSE:
                    LOGGER.trace("Attribute consent will be skipped as the attribute consent policy for service [{}] "
                        + "is disabled for this request", registeredService.getName());
                    return null;
                case UNDEFINED:
                default:
                    LOGGER.trace("Attribute consent policy for service [{}] is undefined", registeredService.getName());
            }
        }
        if (casProperties.getConsent().isActive()) {
            LOGGER.trace("Attribute consent is enabled globally for all requests");
            val required = this.consentEngine.isConsentRequiredFor(service, registeredService, authentication).isRequired();
            return required ? EVENT_ID_CONSENT_REQUIRED : null;
        }
        LOGGER.trace("Attribute consent will be skipped as neither the attribute consent policy for service [{}] "
            + "nor the global CAS consent policy are enabled for this request", registeredService.getName());
        return null;
    }
}
