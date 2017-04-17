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
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

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
public class PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction.class);
    
    private final PrincipalFactory principalFactory;

    public PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction(
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver, final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final PrincipalFactory principalFactory) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.principalFactory = principalFactory;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final String remoteUser = request.getRemoteUser();

        if (StringUtils.hasText(remoteUser)) {
            LOGGER.debug("Remote User [{}] found in HttpServletRequest", remoteUser);
            return new PrincipalBearingCredential(this.principalFactory.createPrincipal(remoteUser));
        }
        return null;
    }
}
