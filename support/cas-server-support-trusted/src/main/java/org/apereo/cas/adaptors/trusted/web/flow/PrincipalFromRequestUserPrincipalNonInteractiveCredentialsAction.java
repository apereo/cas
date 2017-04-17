package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Implementation of the {@link AbstractNonInteractiveCredentialsAction} that looks for a user
 * principal that is set in the {@link HttpServletRequest} and attempts
 * to construct a Principal (and thus a {@link PrincipalBearingCredential}). If it
 * doesn't find one, this class returns and error event which tells the web flow
 * it could not find any credentials.
 *
 * @author Scott Battaglia
 * @since 3.0.5
 */
public class PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction.class);
    private final PrincipalFactory principalFactory;

    public PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction(
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver, final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final PrincipalFactory principalFactory) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.principalFactory = principalFactory;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final Principal principal = request.getUserPrincipal();

        if (principal != null) {
            LOGGER.debug("UserPrincipal [{}] found in HttpServletRequest", principal.getName());
            return new PrincipalBearingCredential(this.principalFactory.createPrincipal(principal.getName()));
        }

        LOGGER.debug("UserPrincipal not found in HttpServletRequest.");
        return null;
    }
}
