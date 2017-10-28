package org.apereo.cas.web.flow;

import org.apereo.cas.logout.LogoutHttpMessage;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logout action for front SLO : find the next eligible service and perform front logout.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class FrontChannelLogoutAction extends AbstractLogoutAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontChannelLogoutAction.class);
    
    private final LogoutManager logoutManager;

    /**
     * Build from the logout manager.
     *
     * @param logoutManager a logout manager.
     */
    public FrontChannelLogoutAction(final LogoutManager logoutManager) {
        this.logoutManager = logoutManager;
    }

    @Override
    protected Event doInternalExecute(final HttpServletRequest request, final HttpServletResponse response,
                                      final RequestContext context) {

        final List<LogoutRequest> logoutRequests = WebUtils.getLogoutRequests(context);
        final Map<LogoutRequest, LogoutHttpMessage> logoutUrls = new HashMap<>();

        if (logoutRequests != null) {
            logoutRequests.stream()
                    .filter(r -> r.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED)
                    .forEach(r -> {
                        LOGGER.debug("Using logout url [{}] for front-channel logout requests", r.getLogoutUrl().toExternalForm());
                        final String logoutMessage = this.logoutManager.createFrontChannelLogoutMessage(r);
                        LOGGER.debug("Front-channel logout message to send is [{}]", logoutMessage);
                        final LogoutHttpMessage msg = new LogoutHttpMessage(r.getLogoutUrl(), logoutMessage, true);
                        logoutUrls.put(r, msg);
                        r.setStatus(LogoutRequestStatus.SUCCESS);
                        r.getService().setLoggedOutAlready(true);
                    });
            
            if (!logoutUrls.isEmpty()) {
                context.getFlowScope().put("logoutUrls", logoutUrls);
                return new EventFactorySupport().event(this, "propagate");
            }
        }
        return new EventFactorySupport().event(this, FINISH_EVENT);
    }
}
