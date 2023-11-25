package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link BasicAuthenticationAction} that extracts basic authN credentials from the request.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
public class BasicAuthenticationAction extends AbstractNonInteractiveCredentialsAction {

    public BasicAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                     final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                     final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val converter = new BasicAuthenticationConverter();
        val token = converter.convert(request);
        return FunctionUtils.doIfNotNull(token, () -> {
            LOGGER.debug("Received basic authentication request from credentials [{}]", token.getPrincipal());
            return new UsernamePasswordCredential(token.getPrincipal().toString(), token.getCredentials().toString());
        });
    }
}
