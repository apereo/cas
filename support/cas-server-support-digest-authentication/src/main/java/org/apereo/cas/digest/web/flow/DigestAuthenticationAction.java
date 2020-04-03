package org.apereo.cas.digest.web.flow;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.digest.DigestCredential;
import org.apereo.cas.digest.DigestHashedCredentialRetriever;
import org.apereo.cas.digest.util.DigestAuthenticationUtils;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.http.credentials.extractor.DigestAuthExtractor;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link DigestAuthenticationAction} that extracts digest authN credentials from the request.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class DigestAuthenticationAction extends AbstractNonInteractiveCredentialsAction {
    private final String nonce = DigestAuthenticationUtils.createNonce();

    private final DigestHashedCredentialRetriever credentialRetriever;

    private final String realm;

    private final String authenticationMethod;

    public DigestAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                      final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                      final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                      final String realm,
                                      final String authenticationMethod,
                                      final DigestHashedCredentialRetriever credentialRetriever) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.realm = realm;
        this.authenticationMethod = authenticationMethod;
        this.credentialRetriever = credentialRetriever;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext requestContext) {
        try {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

            val extractor = new DigestAuthExtractor();
            val webContext = new JEEContext(request, response, new JEESessionStore());

            val credentialsResult = extractor.extract(webContext);
            if (credentialsResult.isEmpty()) {
                response.addHeader(HttpConstants.AUTHENTICATE_HEADER,
                    DigestAuthenticationUtils.createAuthenticateHeader(this.realm, this.authenticationMethod, this.nonce));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return null;
            }

            val credentials = credentialsResult.get();
            LOGGER.debug("Received digest authentication request from credentials [{}] ", credentials);
            val credential = this.credentialRetriever.findCredential(credentials.getUsername(), this.realm);
            LOGGER.trace("Digest credential password on record for [{}] is [{}]", credentials.getUsername(), credential);
            val serverResponse = credentials.calculateServerDigest(true, credential);
            LOGGER.trace("Server digest calculated for [{}] is [{}]", credentials.getUsername(), serverResponse);
            
            val clientResponse = credentials.getToken();
            if (!serverResponse.equals(clientResponse)) {
                LOGGER.trace("Server digest [{}] does not mach [{}]", serverResponse, clientResponse);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return null;
            }
            return new DigestCredential(credentials.getUsername(), this.realm, credentials.getToken());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
