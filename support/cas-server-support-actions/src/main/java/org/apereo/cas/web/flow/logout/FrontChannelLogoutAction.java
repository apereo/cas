package org.apereo.cas.web.flow.logout;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutHttpMessage;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public FrontChannelLogoutAction(final CentralAuthenticationService centralAuthenticationService,
                                    final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                    final ArgumentExtractor argumentExtractor,
                                    final ServicesManager servicesManager,
                                    final LogoutExecutionPlan logoutExecutionPlan,
                                    final CasConfigurationProperties casProperties) {
        super(centralAuthenticationService, ticketGrantingTicketCookieGenerator,
            argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
    }

    @Override
    protected Event doInternalExecute(final HttpServletRequest request,
                                      final HttpServletResponse response,
                                      final RequestContext context) {

        val logoutRequests = WebUtils.getLogoutRequests(context);
        if (logoutRequests == null || logoutRequests.isEmpty()) {
            return getFinishLogoutEvent();
        }

        if (casProperties.getSlo().isDisabled()) {
            LOGGER.debug("Single logout callbacks are disabled");
            return getFinishLogoutEvent();
        }

        val logoutUrls = new HashMap<SingleLogoutRequestContext, LogoutHttpMessage>();
        logoutRequests
            .stream()
            .filter(r -> r.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED)
            .forEach(r -> {
                LOGGER.debug("Using logout url [{}] for front-channel logout requests", r.getLogoutUrl().toExternalForm());
                logoutExecutionPlan.getSingleLogoutServiceMessageHandlers()
                    .stream()
                    .sorted(Comparator.comparing(SingleLogoutServiceMessageHandler::getOrder))
                    .filter(handler -> handler.supports(r.getExecutionRequest(), r.getService()))
                    .forEach(handler -> {
                        val logoutMessage = handler.createSingleLogoutMessage(r);
                        LOGGER.debug("Front-channel logout message to send to [{}] is [{}]", r.getLogoutUrl(), logoutMessage);
                        val msg = new LogoutHttpMessage(r.getLogoutUrl(), logoutMessage.getPayload(), true);
                        logoutUrls.put(r, msg);
                        r.setStatus(LogoutRequestStatus.SUCCESS);
                        r.getService().setLoggedOutAlready(true);
                    });
            });

        if (!logoutUrls.isEmpty()) {
            WebUtils.putLogoutUrls(context, logoutUrls);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROPAGATE);
        }

        return getFinishLogoutEvent();
    }

    private Event getFinishLogoutEvent() {
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_FINISH);
    }
}
