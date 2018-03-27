package org.apereo.cas.adaptors.trusted.web.flow;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.adaptors.trusted.authentication.principal.RemoteRequestPrincipalAttributesExtractor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.Setter;

/**
 * This is {@link BasePrincipalFromNonInteractiveCredentialsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Setter
@Getter
public abstract class BasePrincipalFromNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction implements Ordered {

    /**
     * The principal factory used to construct the final principal.
     */
    protected final PrincipalFactory principalFactory;

    private int order = Integer.MAX_VALUE;

    private final RemoteRequestPrincipalAttributesExtractor principalAttributesExtractor;

    public BasePrincipalFromNonInteractiveCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                            final PrincipalFactory principalFactory, final RemoteRequestPrincipalAttributesExtractor extractor) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.principalFactory = principalFactory;
        this.principalAttributesExtractor = extractor;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final String remoteUser = getRemotePrincipalId(request);
        if (StringUtils.isNotBlank(remoteUser)) {
            LOGGER.debug("User [{}] found in HttpServletRequest", remoteUser);
            final Map<String, Object> attributes = principalAttributesExtractor.getAttributes(request);
            LOGGER.debug("Attributes [{}] found in HttpServletRequest", attributes);
            return new PrincipalBearingCredential(this.principalFactory.createPrincipal(remoteUser, attributes));
        }
        LOGGER.debug("No user found in HttpServletRequest");
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
