package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
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

    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private ServicesManager servicesManager;

    /**
     * Boolean to determine if we will redirect to any url provided in the
     * service request parameter.
     */
    private boolean followServiceRedirects;

    public LogoutAction(final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                        final ServicesManager servicesManager,
                        final boolean followServiceRedirects) {
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.servicesManager = servicesManager;
        this.followServiceRedirects = followServiceRedirects;
    }

    @Override
    protected Event doInternalExecute(final HttpServletRequest request, final HttpServletResponse response,
                                      final RequestContext context) throws Exception {

        boolean needFrontSlo = false;
        final List<LogoutRequest> logoutRequests = WebUtils.getLogoutRequests(context);
        if (logoutRequests != null) {
            // if some logout request must still be attempted
            needFrontSlo = logoutRequests.stream().anyMatch(logoutRequest -> logoutRequest.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED);
        }

        final String service = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        if (this.followServiceRedirects && service != null) {
            final Service webAppService = webApplicationServiceFactory.createService(service);
            final RegisteredService rService = this.servicesManager.findServiceBy(webAppService);

            if (rService != null && rService.getAccessStrategy().isServiceAccessAllowed()) {
                WebUtils.putLogoutRedirectUrl(context, service);
            }
        }

        // there are some front services to logout, perform front SLO
        if (needFrontSlo) {
            return new Event(this, FRONT_EVENT);
        }
        return new Event(this, FINISH_EVENT);
    }

    public void setFollowServiceRedirects(final boolean followServiceRedirects) {
        this.followServiceRedirects = followServiceRedirects;
    }
}
