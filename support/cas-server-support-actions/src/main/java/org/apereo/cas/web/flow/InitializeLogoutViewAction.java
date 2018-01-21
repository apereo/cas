package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link InitializeLogoutViewAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class InitializeLogoutViewAction extends AbstractLogoutAction {
    private final CasConfigurationProperties casProperties;

    public InitializeLogoutViewAction(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Override
    protected Event doInternalExecute(final HttpServletRequest request, final HttpServletResponse response, final RequestContext context) {
        WebUtils.putGoogleAnalyticsTrackingIdIntoFlowScope(context, casProperties.getGoogleAnalytics().getGoogleAnalyticsTrackingId());
        WebUtils.putGeoLocationTrackingIntoFlowScope(context, casProperties.getEvents().isTrackGeolocation());
        WebUtils.putRecaptchaSiteKeyIntoFlowScope(context, casProperties.getGoogleRecaptcha().getSiteKey());
        return null;
    }
}
