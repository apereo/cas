package org.apereo.cas.web.flow.logout;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
@RequiredArgsConstructor
public class LogoutAction extends AbstractLogoutAction {

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private final ServicesManager servicesManager;

    private final LogoutProperties logoutProperties;

    @Override
    protected Event doInternalExecute(final HttpServletRequest request, final HttpServletResponse response,
                                      final RequestContext context) {

        val logoutRequests = WebUtils.getLogoutRequests(context);
        val needFrontSlo = FunctionUtils.doIf(logoutRequests != null,
            () -> logoutRequests
                .stream()
                .anyMatch(logoutRequest -> logoutRequest.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED),
            () -> Boolean.FALSE)
            .get();

        val paramName = logoutProperties.getRedirectParameter();
        LOGGER.trace("Using parameter name [{}] to detect destination service, if any", paramName);
        val service = request.getParameter(paramName);
        LOGGER.trace("Located target service [{}] for redirection after logout", service);

        if (logoutProperties.isFollowServiceRedirects() && StringUtils.isNotBlank(service)) {
            val webAppService = webApplicationServiceFactory.createService(service);
            val rService = this.servicesManager.findServiceBy(webAppService);

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

        if (needFrontSlo) {
            LOGGER.trace("Proceeding forward with front-channel single logout");
            return new Event(this, CasWebflowConstants.TRANSITION_ID_FRONT);
        }
        LOGGER.trace("Moving forward to finish the logout process");
        return new Event(this, CasWebflowConstants.TRANSITION_ID_FINISH);
    }
}
