package org.apereo.cas.web.flow.logout;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.HttpMessage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Logout action for front SLO : find the next eligible service and perform front logout.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
@Slf4j
public class FrontChannelLogoutAction extends AbstractLogoutAction {

    public FrontChannelLogoutAction(final TicketRegistry ticketRegistry,
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
        val logoutRequests = WebUtils.getLogoutRequests(context);
        if (logoutRequests == null || logoutRequests.isEmpty()) {
            return getFinishLogoutEvent();
        }

        if (casProperties.getSlo().isDisabled()) {
            LOGGER.debug("Single logout callbacks are disabled");
            return getFinishLogoutEvent();
        }

        val logoutUrls = new HashMap<SingleLogoutRequestContext, HttpMessage>();
        logoutRequests
            .stream()
            .filter(sloContext -> sloContext.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED)
            .forEach(sloContext -> {
                LOGGER.debug("Using logout url [{}] for front-channel logout requests", sloContext.getLogoutUrl().toExternalForm());
                logoutExecutionPlan.getSingleLogoutServiceMessageHandlers()
                    .stream()
                    .sorted(Comparator.comparing(SingleLogoutServiceMessageHandler::getOrder))
                    .filter(handler -> handler.supports(sloContext.getExecutionRequest(), sloContext.getService()))
                    .forEach(Unchecked.consumer(handler -> {
                        val logoutMessage = handler.createSingleLogoutMessage(sloContext);
                        LOGGER.debug("Front-channel logout message to send to [{}] is [{}]", sloContext.getLogoutUrl(), logoutMessage);
                        val logoutHttpMessage = handler.prepareLogoutHttpMessageToSend(sloContext, logoutMessage);
                        logoutUrls.put(sloContext, logoutHttpMessage);
                        sloContext.setStatus(LogoutRequestStatus.SUCCESS);
                        sloContext.getService().setLoggedOutAlready(true);
                    }));
            });

        if (!logoutUrls.isEmpty()) {
            WebUtils.putLogoutUrls(context, logoutUrls);
            context.getFlowScope().put("logoutPropagationType", casProperties.getSlo().getLogoutPropagationType());
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_PROPAGATE);
        }

        return getFinishLogoutEvent();
    }

    private Event getFinishLogoutEvent() {
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_FINISH);
    }
}
