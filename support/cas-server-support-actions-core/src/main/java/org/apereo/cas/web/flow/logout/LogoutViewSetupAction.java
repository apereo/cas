package org.apereo.cas.web.flow.logout;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.view.DynamicHtmlView;

import lombok.val;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithContentAction;
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

    /**
     * Flow scope attribute to indicate whether flow should
     * continue on from logout and proceed after thr view is rendered.
     * This typically might be the case in delegated authn flows
     * where CAS needs to handle/post back a logout response to an idp. 
     */
    public static final String FLOW_SCOPE_ATTRIBUTE_PROCEED = "enableProceed";

    public LogoutViewSetupAction(final TicketRegistry ticketRegistry,
                                 final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                 final ArgumentExtractor argumentExtractor,
                                 final ServicesManager servicesManager,
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
        val httpAction = (HttpAction) request.getAttribute(HttpAction.class.getName());
        if (httpAction != null) {
            context.getFlowScope().put(FLOW_SCOPE_ATTRIBUTE_PROCEED, Boolean.TRUE);
            if (httpAction instanceof WithContentAction withContentAction) {
                context.getFlowScope().put(DynamicHtmlView.class.getName(), withContentAction.getContent());
            }
        }
        return null;
    }
}
