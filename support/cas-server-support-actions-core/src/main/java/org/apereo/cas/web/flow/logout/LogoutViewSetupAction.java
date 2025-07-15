package org.apereo.cas.web.flow.logout;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.slo.SingleLogoutContinuation;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.view.DynamicHtmlView;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

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
    protected Event doInternalExecute(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        WebUtils.putGeoLocationTrackingIntoFlowScope(context, casProperties.getEvents().getCore().isTrackGeolocation());
        val requestLogoutContinuation = (SingleLogoutContinuation) request.getAttribute(SingleLogoutContinuation.class.getName());
        val conversationLogoutContinuation = context.getConversationScope().get(SingleLogoutContinuation.class.getName(), SingleLogoutContinuation.class);
        Optional.ofNullable(requestLogoutContinuation)
            .or(() -> Optional.ofNullable(conversationLogoutContinuation))
            .ifPresent(continuation -> {
                context.getFlowScope().put(FLOW_SCOPE_ATTRIBUTE_PROCEED, Boolean.TRUE);
                FunctionUtils.doIfNotBlank(continuation.getContent(), cnt -> context.getFlowScope().put(DynamicHtmlView.class.getName(), cnt));
            });
        return null;
    }
}
