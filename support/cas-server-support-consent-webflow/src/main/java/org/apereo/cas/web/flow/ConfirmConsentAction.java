package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentReminderOptions;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.time.temporal.ChronoUnit;

/**
 * This is {@link ConfirmConsentAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class ConfirmConsentAction extends AbstractConsentAction {

    public ConfirmConsentAction(final ServicesManager servicesManager,
                                final AuthenticationServiceSelectionPlan strategies,
                                final ConsentEngine consentEngine,
                                final CasConfigurationProperties casProperties,
                                final AttributeDefinitionStore attributeDefinitionStore,
                                final ConfigurableApplicationContext applicationContext) {
        super(casProperties, servicesManager, strategies, consentEngine, attributeDefinitionStore, applicationContext);
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val webService = WebUtils.getService(requestContext);
        val service = this.authenticationRequestServiceSelectionStrategies.resolveService(webService);
        val registeredService = getRegisteredServiceForConsent(requestContext, service);
        val authentication = WebUtils.getAuthentication(requestContext);
        val optionValue = Integer.parseInt(request.getParameter("option"));
        val option = ConsentReminderOptions.valueOf(optionValue);

        val reminder = Long.parseLong(request.getParameter("reminder"));
        val reminderTimeUnit = request.getParameter("reminderTimeUnit");
        val unit = ChronoUnit.valueOf(reminderTimeUnit.toUpperCase());

        LOGGER.debug("Storing consent decision for service [{}]", service);
        consentEngine.storeConsentDecision(service, registeredService, authentication, reminder, unit, option);
        return new EventFactorySupport().success(this);
    }
}
