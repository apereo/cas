package org.jasig.cas.web.flow;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.logout.LogoutRequestStatus;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.wavity.broker.api.provider.BrokerProvider;
import com.wavity.broker.util.EventAttribute;
import com.wavity.broker.util.EventType;
import com.wavity.broker.util.TopicType;

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
@Component("logoutAction")
public final class LogoutAction extends AbstractLogoutAction {

    /** The services manager. */
    @NotNull
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    /**
     * Boolean to determine if we will redirect to any url provided in the
     * service request parameter.
     */
    @Value("${cas.logout.followServiceRedirects:false}")
    private boolean followServiceRedirects;

    @Override
    protected Event doInternalExecute(final HttpServletRequest request, final HttpServletResponse response,
            final RequestContext context) throws Exception {

        boolean needFrontSlo = false;
        putLogoutIndex(context, 0);
        final List<LogoutRequest> logoutRequests = WebUtils.getLogoutRequests(context);
        if (logoutRequests != null) {
            for (final LogoutRequest logoutRequest : logoutRequests) {
                // if some logout request must still be attempted
                if (logoutRequest.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED) {
                    needFrontSlo = true;
                    break;
                }
            }
        }
        
        // Produce a message using broker API
        produceLogoutMessage(context);
        
        final String service = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        if (this.followServiceRedirects && service != null) {
            final Service webAppService = new WebApplicationServiceFactory().createService(service);
            final RegisteredService rService = this.servicesManager.findServiceBy(webAppService);

            if (rService != null && rService.getAccessStrategy().isServiceAccessAllowed()) {
                context.getFlowScope().put("logoutRedirectUrl", service);
            }
        }
        
        // there are some front services to logout, perform front SLO
        if (needFrontSlo) {
            return new Event(this, FRONT_EVENT);
        } else {
            // otherwise, finish the logout process
            return new Event(this, FINISH_EVENT);
        }
    }

    public void setFollowServiceRedirects(final boolean followServiceRedirects) {
        this.followServiceRedirects = followServiceRedirects;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }
    
    /**
     * Produces a message for logout event
     * 
     * @param context the object of RequestContext
     */
    private void produceLogoutMessage(final RequestContext context) {
    	if (context == null) {
    		logger.error("*** Request context can't be null to produce a logout message ***");
    		return;
    	}
    	final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
    	String tenantId = null;
    	if (request != null) {
			tenantId = AuthUtils.extractTenantID(request);
		}
		if (tenantId == null || "".equals(tenantId)) {
			logger.error("*** Tenant ID can't be empty or null ***");
			return;
		}
		try {
			final BrokerProvider brokerProvider = BrokerProvider.getInstance();
			final EnumMap<EventAttribute, Object> attr = new EnumMap<EventAttribute, Object>(EventAttribute.class);
			attr.put(EventAttribute.MESSAGE, String.format("The user %s logged out", AuthUtils.getCredential()));
			attr.put(EventAttribute.ACTOR_ID, tenantId);
			attr.put(EventAttribute.TIMESTAMP, Long.toString(Calendar.getInstance().getTimeInMillis()));
			attr.put(EventAttribute.IS_NOTIFY_TARGET, true);
			attr.put(EventAttribute.EVENT_RESULT, "success");
			attr.put(EventAttribute.EC_ID, "Test EC ID");
			brokerProvider.publish(TopicType.ADMIN, EventType.EVENT_TYPE_SSO_AUTHENTICATION, attr);
		} catch (Exception e) {
			logger.error("broker failed to publish event", e);
		}
    }
}
