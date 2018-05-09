package org.apereo.cas.web.flow.actions;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link AbstractAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractAuthenticationAction extends AbstractAction {

    private final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;
    private final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;
    private final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    public AbstractAuthenticationAction(final CasDelegatingWebflowEventResolver delegatingWebflowEventResolver,
                                        final CasWebflowEventResolver webflowEventResolver,
                                        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        this.initialAuthenticationAttemptWebflowEventResolver = delegatingWebflowEventResolver;
        this.serviceTicketRequestWebflowEventResolver = webflowEventResolver;
        this.adaptiveAuthenticationPolicy = adaptiveAuthenticationPolicy;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final String agent = WebUtils.getHttpServletRequestUserAgentFromRequestContext();
        final GeoLocationRequest geoLocation = WebUtils.getHttpServletRequestGeoLocationFromRequestContext();

        if (geoLocation != null && StringUtils.isNotBlank(agent) && !adaptiveAuthenticationPolicy.apply(agent, geoLocation)) {
            final String msg = "Adaptive authentication policy does not allow this request for " + agent + " and " + geoLocation;
            final Map<String, Class<? extends Throwable>> map = CollectionUtils.wrap(
                UnauthorizedAuthenticationException.class.getSimpleName(),
                UnauthorizedAuthenticationException.class);
            final AuthenticationException error = new AuthenticationException(msg, map, new HashMap<>(0));
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
    protected void onWarn(final RequestContext context) {
    }

    /**
     * On success.
     *
     * @param context the context
     */
    protected void onSuccess(final RequestContext context) {
    }

    /**
     * On error.
     *
     * @param context the context
     */
    protected void onError(final RequestContext context) {
    }
}
