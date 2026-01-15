package org.apereo.cas.adaptors.trusted.web.flow;

import module java.base;
import org.apereo.cas.adaptors.trusted.authentication.principal.RemoteRequestPrincipalAttributesExtractor;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link PrincipalFromRequestHeaderNonInteractiveCredentialsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class PrincipalFromRequestHeaderNonInteractiveCredentialsAction extends BasePrincipalFromNonInteractiveCredentialsAction {
    private final String remotePrincipalHeader;

    public PrincipalFromRequestHeaderNonInteractiveCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                                     final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                                     final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                                     final PrincipalFactory principalFactory,
                                                                     final RemoteRequestPrincipalAttributesExtractor extractor,
                                                                     final String remotePrincipalHeader) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver,
            adaptiveAuthenticationPolicy, principalFactory, extractor);
        this.remotePrincipalHeader = remotePrincipalHeader;
    }

    @Override
    public String getRemotePrincipalId(final HttpServletRequest request) {
        val principal = request.getUserPrincipal();
        if (principal != null) {
            LOGGER.debug("Principal [{}] found in request", principal.getName());
            return principal.getName();
        }
        val remoteUser = request.getRemoteUser();
        if (StringUtils.isNotBlank(remoteUser)) {
            LOGGER.debug("Remote user [{}] found in HttpServletRequest", remoteUser);
            return remoteUser;
        }

        if (StringUtils.isNotBlank(this.remotePrincipalHeader)) {
            val header = request.getHeader(this.remotePrincipalHeader);
            if (StringUtils.isNotBlank(header)) {
                LOGGER.debug("Remote user [{}] found in [{}] header", header, this.remotePrincipalHeader);
                return header;
            }
        }
        LOGGER.debug("No remote user [{}] could be found", remoteUser);
        return null;
    }
}
