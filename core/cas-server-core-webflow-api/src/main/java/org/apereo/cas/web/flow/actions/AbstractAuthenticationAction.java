package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.HashMap;

/**
 * This is {@link AbstractAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public abstract class AbstractAuthenticationAction extends AbstractAction {

    private final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;
    private final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;
    private final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val agent = WebUtils.getHttpServletRequestUserAgentFromRequestContext();
        val geoLocation = WebUtils.getHttpServletRequestGeoLocationFromRequestContext();

        if (geoLocation != null && StringUtils.isNotBlank(agent)
                && !adaptiveAuthenticationPolicy.apply(requestContext, agent, geoLocation)) {
            val msg = "Adaptive authentication policy does not allow this request for " + agent + " and " + geoLocation;
            val map = CollectionUtils.<String, Throwable>wrap(UnauthorizedAuthenticationException.class.getSimpleName(),
                new UnauthorizedAuthenticationException(msg));
            val error = new AuthenticationException(msg, map, new HashMap<>(0));
            return new Event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                new LocalAttributeMap<>(CasWebflowConstants.TRANSITION_ID_ERROR, error));
        }

        val serviceTicketEvent = this.serviceTicketRequestWebflowEventResolver.resolveSingle(requestContext);
        if (serviceTicketEvent != null) {
            fireEventHooks(serviceTicketEvent, requestContext);
            return serviceTicketEvent;
        }

        val finalEvent = this.initialAuthenticationAttemptWebflowEventResolver.resolveSingle(requestContext);
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
