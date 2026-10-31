package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.BadCredentialsException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20ProofKeyCodeExchangeAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class OAuth20ProofKeyCodeExchangeAuthenticator extends OAuth20ClientIdClientSecretAuthenticator {

    public OAuth20ProofKeyCodeExchangeAuthenticator(
        final ServicesManager servicesManager,
        final ServiceFactory webApplicationServiceFactory,
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        final TicketRegistry ticketRegistry,
        final PrincipalResolver principalResolver,
        final OAuth20RequestParameterResolver requestParameterResolver,
        final OAuth20ClientSecretValidator clientSecretValidator,
        final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
        final TicketFactory ticketFactory,
        final ConfigurableApplicationContext applicationContext) {
        super(servicesManager, webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer,
            ticketRegistry, principalResolver, requestParameterResolver, clientSecretValidator,
            profileScopeToAttributesFilter, ticketFactory, applicationContext);
    }

    @Override
    protected boolean canAuthenticate(final HttpServletRequest request) {
        return getRequestParameterResolver().resolveRequestParameter(request, OAuth20Constants.CODE_VERIFIER).isPresent()
            && getRequestParameterResolver().resolveRequestParameter(request, OAuth20Constants.CODE).isPresent();
    }

    @Override
    protected void validateCredentials(final OAuth20ClientAuthenticationToken credentials,
                                       final OAuthRegisteredService registeredService,
                                       final OAuth20ClientAuthenticationMethods authnMethod) {
        val request = credentials.getRequest();
        val clientSecret = getRequestParameterResolver().resolveClientIdAndClientSecret(request).getRight();
        if (!getClientSecretValidator().validate(registeredService, clientSecret)) {
            throw new BadCredentialsException("Client Credentials provided is not valid for service: " + registeredService.getName());
        }
        val codeVerifier = getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.CODE_VERIFIER)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val code = getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.CODE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);

        LOGGER.debug("Received PKCE code verifier [{}] along with code [{}]", codeVerifier, code);
        val token = OAuth20Utils.getRequestedOAuthToken(request, getTicketRegistry(), code, OAuth20Code.class);
        if (token == null) {
            OAuth20Utils.revokeTokensIssuedFromCode(getTicketRegistry(), code);
            throw new BadCredentialsException("Unknown code: " + code);
        }
        if (token.isExpired()) {
            LOGGER.error("Provided code [{}] is either not found in the ticket registry or has expired", code);
            throw new BadCredentialsException("Invalid token: " + code);
        }

        if (!OAuth20Utils.isCodeVerifierValidFor(token, codeVerifier)) {
            LOGGER.error("Code verifier does not match the challenge of code [{}] using method [{}]", code, token.getCodeChallengeMethod());
            throw new BadCredentialsException("Code verification does not match the challenge assigned to: " + token.getId());
        }
        LOGGER.debug("Validated code verifier using verification method [{}]", token.getCodeChallengeMethod());
    }
}
