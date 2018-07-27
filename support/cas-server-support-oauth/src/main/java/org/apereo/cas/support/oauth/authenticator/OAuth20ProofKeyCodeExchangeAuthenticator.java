package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.CredentialsException;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link OAuth20ProofKeyCodeExchangeAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class OAuth20ProofKeyCodeExchangeAuthenticator extends OAuth20ClientAuthenticator {
    private final TicketRegistry ticketRegistry;

    public OAuth20ProofKeyCodeExchangeAuthenticator(final ServicesManager servicesManager, final ServiceFactory webApplicationServiceFactory,
                                                    final AuditableExecution registeredServiceAccessStrategyEnforcer, final TicketRegistry ticketRegistry) {
        super(servicesManager, webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    protected void validateCredentials(final UsernamePasswordCredentials credentials, final OAuthRegisteredService registeredService, final WebContext context) {
        val codeVerifier = credentials.getPassword();
        val code = context.getRequestParameter(OAuth20Constants.CODE);

        val token = this.ticketRegistry.getTicket(code, OAuthCode.class);
        if (token == null || token.isExpired()) {
            LOGGER.error("Provided code [{}] is either not found in the ticket registry or has expired", code);
            throw new CredentialsException("Invalid token: " + code);
        }

        val method = StringUtils.defaultString(token.getCodeChallengeMethod(), "plain");
        val hash = calculateCodeVerifierHash(method, codeVerifier);
        if (!hash.equalsIgnoreCase(token.getCodeChallenge())) {
            LOGGER.error("Code verifier [{}] does not match the challenge [{}]", hash, token.getCodeChallenge());
            throw new CredentialsException("Code verification does not match the challenge assigned to: " + token.getId());
        }
        LOGGER.debug("Validated code verifier using [{}] verification method [{}]", method);
    }

    private String calculateCodeVerifierHash(final String method, final String codeVerifier) {
        if ("plain".equalsIgnoreCase(method)) {
            return codeVerifier;
        }
        if ("S256".equalsIgnoreCase(method)) {
            val sha256 = DigestUtils.sha256(codeVerifier);
            return EncodingUtils.encodeUrlSafeBase64(sha256.getBytes(StandardCharsets.UTF_8));
        }
        throw new CredentialsException("Code verification method is unrecognized: " + method);
    }
}
