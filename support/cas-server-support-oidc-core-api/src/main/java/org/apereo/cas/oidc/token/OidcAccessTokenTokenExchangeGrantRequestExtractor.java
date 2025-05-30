package org.apereo.cas.oidc.token;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenTokenExchangeGrantRequestExtractor;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link OidcAccessTokenTokenExchangeGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class OidcAccessTokenTokenExchangeGrantRequestExtractor extends AccessTokenTokenExchangeGrantRequestExtractor<OidcConfigurationContext> {
    public OidcAccessTokenTokenExchangeGrantRequestExtractor(final ObjectProvider<OidcConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    @Override
    protected TokenExchangeRequest extractSubjectTokenExchangeRequest(final WebContext webContext) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val requestParameterResolver = configurationContext.getRequestParameterResolver();
        val subjectTokenType = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN_TYPE)
            .map(OAuth20TokenExchangeTypes::from)
            .orElseThrow();
        val subjectToken = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN)
            .orElseThrow(() -> new IllegalArgumentException("Subject token cannot be undefined"));

        if (subjectTokenType == OAuth20TokenExchangeTypes.ID_TOKEN) {
            val clientIdInIdToken = OAuth20Utils.extractClientIdFromToken(subjectToken);
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientIdInIdToken);
            val claimSet = configurationContext.getIdTokenSigningAndEncryptionService().decode(subjectToken, Optional.ofNullable(registeredService));
            val service = configurationContext.getWebApplicationServiceServiceFactory().createService(claimSet.getIssuer());
            service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(claimSet.getIssuer()));
            val userProfile = extractUserProfile(webContext).orElseThrow();
            userProfile.setId(claimSet.getSubject());
            claimSet.getClaimsMap().forEach(userProfile::addAttribute);
            val authentication = configurationContext.getAuthenticationBuilder().build(userProfile, registeredService, webContext, service);
            val accessTokenFactory = (OAuth20AccessTokenFactory) configurationContext.getTicketFactory().get(OAuth20AccessToken.class);
            val scopes = configurationContext.getRequestParameterResolver().resolveRequestedScopes(webContext);
            val accessToken = accessTokenFactory.create(service, authentication, scopes, clientIdInIdToken,
                OAuth20ResponseTypes.NONE, OAuth20GrantTypes.TOKEN_EXCHANGE);
            return new TokenExchangeRequest(accessToken, service, registeredService, authentication);

        }
        return super.extractSubjectTokenExchangeRequest(webContext);
    }

    @Override
    protected Authentication getActorTokenAuthentication(final WebContext webContext, final TokenExchangeRequest extractedRequest) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val actorToken = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(webContext, OAuth20Constants.ACTOR_TOKEN);
        val actorTokenType = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(webContext, OAuth20Constants.ACTOR_TOKEN_TYPE)
            .map(OAuth20TokenExchangeTypes::from);

        if (actorTokenType.isPresent() && actorTokenType.get() == OAuth20TokenExchangeTypes.DEVICE_SECRET) {
            val subjectTokenType = configurationContext.getRequestParameterResolver()
                .resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN_TYPE)
                .map(OAuth20TokenExchangeTypes::from);
            FunctionUtils.throwIf(subjectTokenType.isEmpty() || subjectTokenType.get() != OAuth20TokenExchangeTypes.ID_TOKEN,
                () -> new IllegalArgumentException("Subject token type is missing or not an ID token when actor token is %s".formatted(actorTokenType.get())));
            val subjectToken = configurationContext.getRequestParameterResolver()
                .resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN)
                .orElseThrow(() -> new IllegalArgumentException("Subject token type cannot be undefined when actor token is provided"));
            val clientIdInIdToken = OAuth20Utils.extractClientIdFromToken(subjectToken);
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientIdInIdToken);
            val claims = configurationContext.getIdTokenSigningAndEncryptionService().decode(subjectToken, Optional.ofNullable(registeredService));
            Assert.isTrue(claims.hasClaim(OidcConstants.DS_HASH), "Subject token must contain the claim %s".formatted(OidcConstants.DS_HASH));
            Assert.isTrue(claims.hasClaim(OidcConstants.CLAIM_SESSION_ID), "Subject token must contain the claim %s".formatted(OidcConstants.CLAIM_SESSION_ID));
            Assert.isTrue(claims.hasClaim(OidcConstants.CLAIM_SESSION_REF), "Subject token must contain the claim %s".formatted(OidcConstants.CLAIM_SESSION_REF));

            val deviceSecret = configurationContext.getDeviceSecretGenerator().hash(actorToken.orElseThrow());
            Assert.isTrue(StringUtils.equals(deviceSecret, claims.getStringClaimValue(OidcConstants.DS_HASH)),
                "Device secret hash does not match the subject token claim %s".formatted(OidcConstants.DS_HASH));

            val base64DecodedSessionId = EncodingUtils.decodeUrlSafeBase64(claims.getStringClaimValue(OidcConstants.CLAIM_SESSION_REF));
            val ticketGrantingTicketId = new String((byte[]) configurationContext.getTicketRegistry().getCipherExecutor().decode(base64DecodedSessionId), StandardCharsets.UTF_8);
            val ticketGrantingTicket = configurationContext.getTicketRegistry().getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
            Assert.notNull(ticketGrantingTicket, "Ticket granting ticket cannot be null");
            return ticketGrantingTicket.getAuthentication();
        }
        return super.getActorTokenAuthentication(webContext, extractedRequest);
    }
}
