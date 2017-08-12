package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.adaptors.trusted.authentication.principal.RemoteRequestPrincipalAttributesExtractor;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction extends BasePrincipalFromNonInteractiveCredentialsAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction.class);

    public PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction(
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final PrincipalFactory principalFactory,
            final RemoteRequestPrincipalAttributesExtractor extractor) {
        super(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy,
                principalFactory, extractor);
    }

    @Override
    protected String getRemotePrincipalId(final HttpServletRequest request) {
        final Principal principal = request.getUserPrincipal();

        if (principal != null) {
            LOGGER.debug("Principal [{}] found in HttpServletRequest", principal.getName());
            return principal.getName();
        }
        return null;
    }
}
