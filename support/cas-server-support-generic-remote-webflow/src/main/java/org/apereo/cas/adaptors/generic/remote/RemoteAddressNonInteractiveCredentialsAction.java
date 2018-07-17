package org.apereo.cas.adaptors.generic.remote;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * A webflow action that attempts to grab the remote address from the request,
 * and construct a {@link RemoteAddressCredential} object.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Slf4j
public class RemoteAddressNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {


    public RemoteAddressNonInteractiveCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val remoteAddress = request.getRemoteAddr();

        if (StringUtils.hasText(remoteAddress)) {
            return new RemoteAddressCredential(remoteAddress);
        }

        LOGGER.debug("No remote address found.");
        return null;
    }
}
