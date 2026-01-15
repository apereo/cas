package org.apereo.cas.web.flow.logout;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action to delete the TGT and the appropriate cookies.
 * It also performs the back-channel SLO on the services accessed by the user during its browsing.
 * After this back-channel SLO, a front-channel SLO can be started if some services require it.
 * The final logout page or a redirection url is also computed in this action.
 *
 * @author Scott Battaglia
 * @author Jerome Leleu
 * @since 3.0.0
 */
@Slf4j
public class LogoutAction extends AbstractLogoutAction {

    public LogoutAction(final TicketRegistry ticketRegistry,
                        final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                        final ArgumentExtractor argumentExtractor, final ServicesManager servicesManager,
                        final LogoutExecutionPlan logoutExecutionPlan, final CasConfigurationProperties casProperties) {
        super(ticketRegistry, ticketGrantingTicketCookieGenerator,
            argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
    }

    @Override
    protected Event doInternalExecute(final RequestContext requestContext) {
        val logoutRequests = WebUtils.getLogoutRequests(requestContext);
        val needFrontSlo = FunctionUtils.doIf(logoutRequests != null,
                () -> Objects.requireNonNull(logoutRequests)
                    .stream()
                    .anyMatch(logoutRequest -> logoutRequest.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED),
                () -> Boolean.FALSE)
            .get();

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        logoutExecutionPlan.getLogoutRedirectionStrategies()
            .stream()
            .filter(strategy -> strategy.supports(request, response))
            .map(Unchecked.function(strategy -> strategy.handle(request, response)))
            .filter(Objects::nonNull)
            .forEach(logoutResponse -> {
                LOGGER.debug("Logout response is [{}]", logoutResponse);
                logoutResponse.getService().ifPresent(service -> WebUtils.putServiceIntoFlowScope(requestContext, service));
                logoutResponse.getLogoutRedirectUrl().ifPresent(url -> WebUtils.putLogoutRedirectUrl(requestContext, url));
                logoutResponse.getLogoutPostUrl().ifPresent(url -> WebUtils.putLogoutPostUrl(requestContext, url));
                if (!logoutResponse.getLogoutPostData().isEmpty()) {
                    WebUtils.putLogoutPostData(requestContext, logoutResponse.getLogoutPostData());
                }
            });

        if (needFrontSlo) {
            LOGGER.trace("Proceeding forward with front-channel single logout");
            return new Event(this, CasWebflowConstants.TRANSITION_ID_FRONT);
        }
        LOGGER.trace("Moving forward to finish the logout process");
        return new Event(this, CasWebflowConstants.TRANSITION_ID_FINISH);
    }
}
