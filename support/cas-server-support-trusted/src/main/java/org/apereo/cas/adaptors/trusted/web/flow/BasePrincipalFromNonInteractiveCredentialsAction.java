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
 * This is {@link BasePrincipalFromNonInteractiveCredentialsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BasePrincipalFromNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasePrincipalFromNonInteractiveCredentialsAction.class);

    /**
     * The principal factory used to construct the final principal.
     */
    protected final PrincipalFactory principalFactory;

    public BasePrincipalFromNonInteractiveCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                            final PrincipalFactory principalFactory) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.principalFactory = principalFactory;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final String remoteUser = getRemotePrincipalId(request);

        if (StringUtils.hasText(remoteUser)) {
            LOGGER.debug("Remote User [{}] found in HttpServletRequest", remoteUser);


            return new PrincipalBearingCredential(this.principalFactory.createPrincipal(remoteUser));
        }
        return null;
    }

    /**
     * Gets remote principal id.
     *
     * @param request the request
     * @return the remote principal id
     */
    protected abstract String getRemotePrincipalId(HttpServletRequest request);
}
