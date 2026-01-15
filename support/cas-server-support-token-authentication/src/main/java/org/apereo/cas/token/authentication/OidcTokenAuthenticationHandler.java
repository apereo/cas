package org.apereo.cas.token.authentication;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.token.TokenAuthenticationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;

/**
 * This is {@link OidcTokenAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class OidcTokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private final ObjectProvider<OidcConfigurationContext> configurationContextProvider;

    public OidcTokenAuthenticationHandler(
        final PrincipalFactory principalFactory,
        final ObjectProvider<OidcConfigurationContext> configurationContextProvider,
        final TokenAuthenticationProperties properties) {
        super(StringUtils.EMPTY, principalFactory, properties.getOrder() - 1);
        this.configurationContextProvider = configurationContextProvider;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(
        final Credential credential, final Service service) {
        val tokenCredential = (BasicIdentifiableCredential) credential;
        return attemptAuthenticationWithAccessToken(tokenCredential)
            .or(() -> attemptAuthenticationWithIdToken(tokenCredential))
            .orElseThrow(() -> new AuthenticationException("Unable to authenticate token credential"));
    }

    protected Optional<AuthenticationHandlerExecutionResult> attemptAuthenticationWithIdToken(
        final BasicIdentifiableCredential tokenCredential) {
        try {
            if (JwtBuilder.tryParse(tokenCredential.getId()).isPresent()) {
                val clientIdInIdToken = OAuth20Utils.extractClientIdFromToken(tokenCredential.getId());
                LOGGER.debug("Client id retrieved from ID token is [{}]", clientIdInIdToken);

                val configurationContext = configurationContextProvider.getObject();
                val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientIdInIdToken);
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
                val claims = configurationContext.getIdTokenSigningAndEncryptionService().decode(tokenCredential.getId(), Optional.ofNullable(registeredService));
                val principalId = claims.getSubject();
                val principal = principalFactory.createPrincipal(principalId, (Map) claims.getClaimsMap());
                tokenCredential.setId(Objects.requireNonNull(principal).getId());
                return Optional.of(createHandlerResult(tokenCredential, principal, new ArrayList<>()));
            }
        } catch (final Throwable e) {
            LOGGER.trace("Unable to authenticate id token credential", e);
        }
        return Optional.empty();
    }

    protected Optional<AuthenticationHandlerExecutionResult> attemptAuthenticationWithAccessToken(
        final BasicIdentifiableCredential tokenCredential) {
        try {
            val configurationContext = configurationContextProvider.getObject();
            val accessTokenId = Objects.requireNonNull(OAuth20JwtAccessTokenEncoder.toDecodableCipher(
                configurationContext.getAccessTokenJwtBuilder()).decode(tokenCredential.getId()));
            val decodedToken = configurationContext.getTicketRegistry().getTicket(accessTokenId, OAuth20AccessToken.class);
            Assert.isTrue(decodedToken != null && !decodedToken.isExpired()
                && Objects.nonNull(decodedToken.getAuthentication()), "Access token is invalid or expired");
            val claims = configurationContext.getUserProfileDataCreator().createFrom(decodedToken);
            val attributes = CollectionUtils.toMultiValuedMap((Map) claims.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES));
            val principalId = decodedToken.getAuthentication().getPrincipal().getId();
            val principal = principalFactory.createPrincipal(principalId, attributes);
            tokenCredential.setId(Objects.requireNonNull(principal).getId());
            return Optional.of(createHandlerResult(tokenCredential, principal, new ArrayList<>()));
        } catch (final Throwable e) {
            LOGGER.trace("Unable to authenticate access token credential", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof BasicIdentifiableCredential;
    }
}

