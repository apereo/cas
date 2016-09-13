package org.apereo.cas.web.flow;

import com.google.common.collect.ImmutableMap;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collections;
import java.util.Map;

/**
 * Action to authenticate credential and retrieve a TicketGrantingTicket for
 * those credential. If there is a request for renew, then it also generates
 * the Service Ticket required.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class AuthenticationViaFormAction extends AbstractAction {

    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final String agent = WebUtils.getHttpServletRequestUserAgent();
        final GeoLocationRequest geoLocation = WebUtils.getHttpServletRequestGeoLocation();

        if (!adaptiveAuthenticationPolicy.apply(agent, geoLocation)) {
            final String msg = "Adaptive authentication policy does not allow this request for " + agent + " and " + geoLocation;
            final Map map = ImmutableMap.of(
                    UnauthorizedAuthenticationException.class.getSimpleName(), 
                    UnauthorizedAuthenticationException.class);
            final AuthenticationException error = new AuthenticationException(msg, map, Collections.emptyMap());
            return new Event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                    new LocalAttributeMap(CasWebflowConstants.TRANSITION_ID_ERROR, error));
            
        }

        final Event serviceTicketEvent = this.serviceTicketRequestWebflowEventResolver.resolveSingle(requestContext);
        if (serviceTicketEvent != null) {
            return serviceTicketEvent;
        }
        return this.initialAuthenticationAttemptWebflowEventResolver.resolveSingle(requestContext);
        
    }

    public void setServiceTicketRequestWebflowEventResolver(final CasWebflowEventResolver r) {
        this.serviceTicketRequestWebflowEventResolver = r;
    }

    public void setInitialAuthenticationAttemptWebflowEventResolver(final CasWebflowEventResolver r) {
        this.initialAuthenticationAttemptWebflowEventResolver = r;
    }

    public void setAdaptiveAuthenticationPolicy(final AdaptiveAuthenticationPolicy a) {
        this.adaptiveAuthenticationPolicy = a;
    }
}
