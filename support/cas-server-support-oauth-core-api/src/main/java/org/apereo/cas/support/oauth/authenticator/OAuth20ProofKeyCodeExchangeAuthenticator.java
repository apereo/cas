package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.CredentialsException;

import java.io.Serializable;

/**
 * This is {@link OAuth20ProofKeyCodeExchangeAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class OAuth20ProofKeyCodeExchangeAuthenticator extends OAuth20ClientIdClientSecretAuthenticator {

    public OAuth20ProofKeyCodeExchangeAuthenticator(final ServicesManager servicesManager,
                                                    final ServiceFactory webApplicationServiceFactory,
                                                    final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                    final TicketRegistry ticketRegistry,
                                                    final CipherExecutor<Serializable, String> registeredServiceCipherExecutor,
                                                    final PrincipalResolver principalResolver) {
        super(servicesManager, webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer,
            registeredServiceCipherExecutor, ticketRegistry, principalResolver);
    }

    @Override
    protected boolean canAuthenticate(final WebContext context) {
        return context.getRequestParameter(OAuth20Constants.CODE_VERIFIER).isPresent();
    }

    @Override
    protected void validateCredentials(final UsernamePasswordCredentials credentials,
                                       final OAuthRegisteredService registeredService, final WebContext context) {
        val clientSecret = OAuth20Utils.getClientIdAndClientSecret(context).getRight();

        if (!OAuth20Utils.checkClientSecret(registeredService, clientSecret, getRegisteredServiceCipherExecutor())) {
            throw new CredentialsException("Client Credentials provided is not valid for registered service: " + registeredService.getName());
        }

        val codeVerifier = context.getRequestParameter(OAuth20Constants.CODE_VERIFIER)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val code = context.getRequestParameter(OAuth20Constants.CODE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);

        val token = getTicketRegistry().getTicket(code, OAuth20Code.class);
        if (token == null || token.isExpired()) {
            LOGGER.error("Provided code [{}] is either not found in the ticket registry or has expired", code);
            throw new CredentialsException("Invalid token: " + code);
        }

        val method = StringUtils.defaultIfEmpty(token.getCodeChallengeMethod(), "plain");
        val hash = calculateCodeVerifierHash(method, codeVerifier);
        if (!hash.equalsIgnoreCase(token.getCodeChallenge())) {
            LOGGER.error("Code verifier [{}] does not match the challenge [{}]", hash, token.getCodeChallenge());
            throw new CredentialsException("Code verification does not match the challenge assigned to: " + token.getId());
        }
        LOGGER.debug("Validated code verifier using verification method [{}]", method);
    }

    private static String calculateCodeVerifierHash(final String method, final String codeVerifier) {
        if ("plain".equalsIgnoreCase(method)) {
            return codeVerifier;
        }
        if ("S256".equalsIgnoreCase(method)) {
            val sha256 = DigestUtils.rawDigestSha256(codeVerifier);
            return EncodingUtils.encodeUrlSafeBase64(sha256);
        }
        throw new CredentialsException("Code verification method is unrecognized: " + method);
    }
}
