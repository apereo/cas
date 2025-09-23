package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class AbstractAuthenticationAction extends BaseCasWebflowAction {

    private final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    private final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    private final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        if (!evaluateAdaptiveAuthenticationPolicy(requestContext)) {
            val agent = WebUtils.getHttpServletRequestUserAgentFromRequestContext(requestContext);
            val geoLocation = WebUtils.getHttpServletRequestGeoLocationFromRequestContext(requestContext);

            val msg = "Adaptive authentication policy does not allow this request for " + agent + " and " + geoLocation;
            LOGGER.warn(msg);
            val map = CollectionUtils.<String, Throwable>wrap(UnauthorizedAuthenticationException.class.getSimpleName(),
                new UnauthorizedAuthenticationException(msg));
            val error = new AuthenticationException(msg, map, new HashMap<>());
            val event = new Event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                new LocalAttributeMap<>(CasWebflowConstants.TRANSITION_ID_ERROR, error));
            fireEventHooks(event, requestContext);
            return event;
        }

        val serviceTicketEvent = serviceTicketRequestWebflowEventResolver.resolveSingle(requestContext);
        if (serviceTicketEvent != null) {
            fireEventHooks(serviceTicketEvent, requestContext);
            return serviceTicketEvent;
        }

        val finalEvent = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(requestContext);
        fireEventHooks(finalEvent, requestContext);
        return finalEvent;
    }

    protected boolean evaluateAdaptiveAuthenticationPolicy(final RequestContext requestContext) throws Throwable {
        val agent = WebUtils.getHttpServletRequestUserAgentFromRequestContext(requestContext);
        val geoLocation = WebUtils.getHttpServletRequestGeoLocationFromRequestContext(requestContext);
        return adaptiveAuthenticationPolicy.isAuthenticationRequestAllowed(requestContext, agent, geoLocation);
    }

    protected Event fireEventHooks(final Event event, final RequestContext ctx) {
        val id = event.getId();
        if (id.equals(CasWebflowConstants.TRANSITION_ID_ERROR) || id.equals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE)) {
            onError(ctx);
        }
        if (id.equals(CasWebflowConstants.TRANSITION_ID_WARN)) {
            onWarn(ctx);
        }
        if (id.equals(CasWebflowConstants.TRANSITION_ID_SUCCESS)) {
            onSuccess(ctx);
        }
        return event;
    }

    protected void onWarn(final RequestContext context) {
    }

    protected void onSuccess(final RequestContext context) {
    }

    protected void onError(final RequestContext context) {
    }
}
