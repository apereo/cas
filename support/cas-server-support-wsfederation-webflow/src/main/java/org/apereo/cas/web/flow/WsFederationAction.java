package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.flow.actions.AbstractAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This class represents an action in the webflow to retrieve WsFederation information on the callback url which is
 * the webflow url (/login).
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Slf4j
@Getter
@Setter
public class WsFederationAction extends AbstractAuthenticationAction {
    private static final String WA = "wa";
    private static final String WSIGNIN = "wsignin1.0";

    private final WsFederationResponseValidator wsFederationResponseValidator;
    private final WsFederationRequestBuilder wsFederationRequestBuilder;

    public WsFederationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                              final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                              final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                              final WsFederationRequestBuilder wsFederationRequestBuilder,
                              final WsFederationResponseValidator wsFederationResponseValidator) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.wsFederationResponseValidator = wsFederationResponseValidator;
        this.wsFederationRequestBuilder = wsFederationRequestBuilder;
    }

    /**
     * Executes the webflow action.
     *
     * @param context the context
     * @return the event
     */
    @Override
    protected Event doExecute(final RequestContext context) {
        try {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            val wa = request.getParameter(WA);
            if (StringUtils.isNotBlank(wa) && wa.equalsIgnoreCase(WSIGNIN)) {
                wsFederationResponseValidator.validateWsFederationAuthenticationRequest(context);
                return super.doExecute(context);
            }
            return wsFederationRequestBuilder.buildAuthenticationRequestEvent(context);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, ex.getMessage());
        }
    }
}
