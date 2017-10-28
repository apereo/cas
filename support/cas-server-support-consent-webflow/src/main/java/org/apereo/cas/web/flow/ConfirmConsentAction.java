package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentOptions;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.time.temporal.ChronoUnit;

/**
 * This is {@link ConfirmConsentAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ConfirmConsentAction extends AbstractConsentAction {

    public ConfirmConsentAction(final ServicesManager servicesManager,
                                final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                final ConsentEngine consentEngine,
                                final CasConfigurationProperties casProperties) {
        super(casProperties, servicesManager, authenticationRequestServiceSelectionStrategies, consentEngine);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final Service service = this.authenticationRequestServiceSelectionStrategies.resolveService(WebUtils.getService(requestContext));
        final RegisteredService registeredService = getRegisteredServiceForConsent(requestContext, service);
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final int optionValue = Integer.parseInt(request.getParameter("option"));
        final ConsentOptions option = ConsentOptions.valueOf(optionValue);

        final long reminder = Long.parseLong(request.getParameter("reminder"));
        final String reminderTimeUnit = request.getParameter("reminderTimeUnit");
        final ChronoUnit unit = ChronoUnit.valueOf(reminderTimeUnit.toUpperCase());

        consentEngine.storeConsentDecision(service, registeredService, authentication, reminder, unit, option);
        return new EventFactorySupport().success(this);
    }
}
