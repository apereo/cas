package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentReminderOptions;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
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
                                final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                final ConsentEngine consentEngine,
                                final CasConfigurationProperties casProperties) {
        super(casProperties, servicesManager, authenticationRequestServiceSelectionStrategies, consentEngine);
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final var webService = WebUtils.getService(requestContext);
        final var service = this.authenticationRequestServiceSelectionStrategies.resolveService(webService);
        final var registeredService = getRegisteredServiceForConsent(requestContext, service);
        final var authentication = WebUtils.getAuthentication(requestContext);
        final var optionValue = Integer.parseInt(request.getParameter("option"));
        final var option = ConsentReminderOptions.valueOf(optionValue);

        final var reminder = Long.parseLong(request.getParameter("reminder"));
        final var reminderTimeUnit = request.getParameter("reminderTimeUnit");
        final var unit = ChronoUnit.valueOf(reminderTimeUnit.toUpperCase());

        consentEngine.storeConsentDecision(service, registeredService, authentication, reminder, unit, option);
        return new EventFactorySupport().success(this);
    }
}
