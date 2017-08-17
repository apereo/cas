package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.adaptors.trusted.authentication.principal.RemoteRequestPrincipalAttributesExtractor;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of the {@link AbstractNonInteractiveCredentialsAction} that looks for a user
 * principal that is set in the {@link HttpServletRequest} and attempts
 * to construct a Principal (and thus a {@link PrincipalBearingCredential}). If it
 * doesn't find one, this class returns and error event which tells the web flow
 * it could not find any credentials.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction extends BasePrincipalFromNonInteractiveCredentialsAction {
    public PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction(final CasDelegatingWebflowEventResolver initialWebflowEventResolver,
                                                                         final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                                         final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                                         final PrincipalFactory principalFactory,
                                                                         final RemoteRequestPrincipalAttributesExtractor extractor) {
        super(initialWebflowEventResolver, serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy, principalFactory, extractor);
    }

    @Override
    protected String getRemotePrincipalId(final HttpServletRequest request) {
        return request.getRemoteUser();
    }
}
