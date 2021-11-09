package org.apereo.cas.web.flow.logout;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

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

    public LogoutAction(final CentralAuthenticationService centralAuthenticationService,
                        final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                        final ArgumentExtractor argumentExtractor, final ServicesManager servicesManager,
                        final LogoutExecutionPlan logoutExecutionPlan, final CasConfigurationProperties casProperties) {
        super(centralAuthenticationService, ticketGrantingTicketCookieGenerator,
            argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
    }

    @Override
    protected Event doInternalExecute(final HttpServletRequest request,
                                      final HttpServletResponse response,
                                      final RequestContext context) {

        val logoutRequests = WebUtils.getLogoutRequests(context);
        val needFrontSlo = FunctionUtils.doIf(logoutRequests != null,
                () -> Objects.requireNonNull(logoutRequests)
                    .stream()
                    .anyMatch(logoutRequest -> logoutRequest.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED),
                () -> Boolean.FALSE)
            .get();

        logoutExecutionPlan.getLogoutRedirectionStrategies()
            .stream()
            .filter(s -> s.supports(context))
            .forEach(s -> s.handle(context));

        if (needFrontSlo) {
            LOGGER.trace("Proceeding forward with front-channel single logout");
            return new Event(this, CasWebflowConstants.TRANSITION_ID_FRONT);
        }
        LOGGER.trace("Moving forward to finish the logout process");
        return new Event(this, CasWebflowConstants.TRANSITION_ID_FINISH);
    }
}
