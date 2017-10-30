package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
public class LogoutAction extends AbstractLogoutAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutAction.class);

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private final ServicesManager servicesManager;

    private final LogoutProperties logoutProperties;

    public LogoutAction(final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                        final ServicesManager servicesManager,
                        final LogoutProperties logoutProperties) {
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.servicesManager = servicesManager;
        this.logoutProperties = logoutProperties;
    }

    @Override
    protected Event doInternalExecute(final HttpServletRequest request, final HttpServletResponse response,
                                      final RequestContext context) {

        boolean needFrontSlo = false;
        final List<LogoutRequest> logoutRequests = WebUtils.getLogoutRequests(context);
        if (logoutRequests != null) {
            needFrontSlo = logoutRequests
                    .stream()
                    .anyMatch(logoutRequest -> logoutRequest.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED);
        }

        final String paramName = StringUtils.defaultIfEmpty(logoutProperties.getRedirectParameter(), CasProtocolConstants.PARAMETER_SERVICE);
        LOGGER.debug("Using parameter name [{}] to detect destination service, if any", paramName);
        final String service = request.getParameter(paramName);
        LOGGER.debug("Located target service [{}] for redirection after logout", paramName);

        if (logoutProperties.isFollowServiceRedirects() && StringUtils.isNotBlank(service)) {
            final Service webAppService = webApplicationServiceFactory.createService(service);
            final RegisteredService rService = this.servicesManager.findServiceBy(webAppService);

            if (rService != null && rService.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.debug("Redirecting to service [{}]", service);
                WebUtils.putLogoutRedirectUrl(context, service);
            } else {
                LOGGER.warn("Cannot redirect to [{}] given the service is unauthorized to use CAS. "
                        + "Ensure the service is registered with CAS and is enabled to allowed access", service);
            }
        } else {
            LOGGER.debug("No target service is located for redirection after logout, or CAS is not allowed to follow redirects after logout");
        }

        // there are some front services to logout, perform front SLO
        if (needFrontSlo) {
            LOGGER.debug("Proceeding forward with front-channel single logout");
            return new Event(this, FRONT_EVENT);
        }
        LOGGER.debug("Moving forward to finish the logout process");
        return new Event(this, FINISH_EVENT);
    }
}
