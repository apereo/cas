package org.apereo.cas.web.flow.logout;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link LogoutViewSetupAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class LogoutViewSetupAction extends AbstractLogoutAction {

    public LogoutViewSetupAction(final TicketRegistry ticketRegistry,
                                 final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                 final ArgumentExtractor argumentExtractor, final ServicesManager servicesManager,
                                 final LogoutExecutionPlan logoutExecutionPlan,
                                 final CasConfigurationProperties casProperties) {
        super(ticketRegistry, ticketGrantingTicketCookieGenerator,
            argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
    }

    @Override
    protected Event doInternalExecute(final HttpServletRequest request, final HttpServletResponse response,
                                      final RequestContext context) {
        WebUtils.putGeoLocationTrackingIntoFlowScope(context,
            casProperties.getEvents().getCore().isTrackGeolocation());
        return null;
    }
}
