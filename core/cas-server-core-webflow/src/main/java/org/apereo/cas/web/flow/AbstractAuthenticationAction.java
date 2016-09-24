package org.apereo.cas.web.flow;

import com.google.common.collect.ImmutableMap;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collections;
import java.util.Map;

/**
 * This is {@link AbstractAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractAuthenticationAction extends AbstractAction {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

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
            fireEventHooks(serviceTicketEvent, requestContext);
            return serviceTicketEvent;
        }
        
        final Event finalEvent = this.initialAuthenticationAttemptWebflowEventResolver.resolveSingle(requestContext);
        fireEventHooks(finalEvent, requestContext);
        return finalEvent;
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

    private void fireEventHooks(final Event e, final RequestContext ctx) {
        if (e.getId().equals(CasWebflowConstants.TRANSITION_ID_ERROR)) {
            onError(ctx);
        }
        if (e.getId().equals(CasWebflowConstants.TRANSITION_ID_WARN)) {
            onWarn(ctx);
        }
        if (e.getId().equals(CasWebflowConstants.TRANSITION_ID_SUCCESS)) {
            onSuccess(ctx);
        }
    }

    /**
     * On warn.
     *
     * @param context the context
     */
    protected void onWarn(final RequestContext context) {}

    /**
     * On success.
     *
     * @param context the context
     */
    protected void onSuccess(final RequestContext context) {}

    /**
     * On error.
     *
     * @param context the context
     */
    protected void onError(final RequestContext context) {}
}
