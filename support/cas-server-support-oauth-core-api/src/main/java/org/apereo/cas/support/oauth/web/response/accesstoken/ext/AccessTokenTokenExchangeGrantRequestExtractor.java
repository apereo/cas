package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.springframework.beans.factory.ObjectProvider;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link AccessTokenTokenExchangeGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class AccessTokenTokenExchangeGrantRequestExtractor<T extends OAuth20ConfigurationContext> extends BaseAccessTokenGrantRequestExtractor<T> {
    public AccessTokenTokenExchangeGrantRequestExtractor(final ObjectProvider<T> configurationContext) {
        super(configurationContext);
    }

    @Override
    public boolean supports(final WebContext context) {
        val grantType = getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isGrantType(grantType, getGrantType());
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.TOKEN_EXCHANGE;
    }

    @Override
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.NONE;
    }

    @Override
    protected AccessTokenRequestContext extractRequest(final WebContext webContext) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();

        val requestParameterResolver = configurationContext.getRequestParameterResolver();
        val scopes = requestParameterResolver.resolveRequestScopes(webContext);

        val requestedTokenType = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.REQUESTED_TOKEN_TYPE)
            .orElseGet(OAuth20TokenExchangeTypes.ACCESS_TOKEN::getType);
        val subjectTokenType = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN_TYPE)
            .map(OAuth20TokenExchangeTypes::from)
            .orElseThrow(() -> new IllegalArgumentException("Subject token type cannot be undefined"));
        val resource = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.RESOURCE);
        val audience = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.AUDIENCE).orElse(null);

        val extractedRequest = extractSubjectTokenExchangeRequest(webContext);
        val resourceService = resource
            .map(res -> {
                val service = configurationContext.getWebApplicationServiceServiceFactory().createService(res);
                service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(extractedRequest.registeredService().getClientId()));
                return service;
            })
            .orElse(null);

        return AccessTokenRequestContext
            .builder()
            .scopes(scopes)
            .grantType(getGrantType())
            .subjectTokenType(subjectTokenType)
            .requestedTokenType(OAuth20TokenExchangeTypes.from(requestedTokenType))
            .subjectToken(extractedRequest.token())
            .tokenExchangeResource(resourceService)
            .tokenExchangeAudience(audience)
            .service(extractedRequest.service())
            .registeredService(extractedRequest.registeredService())
            .authentication(extractedRequest.authentication())
            .actorToken(getActorTokenAuthentication(webContext, extractedRequest))
            .build();
    }

    protected TokenExchangeRequest extractSubjectTokenExchangeRequest(final WebContext webContext) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val requestParameterResolver = configurationContext.getRequestParameterResolver();
        val subjectTokenType = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN_TYPE)
            .map(OAuth20TokenExchangeTypes::from)
            .orElseThrow();

        val subjectToken = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN)
            .orElseThrow(() -> new IllegalArgumentException("Subject token cannot be undefined"));
        return switch (subjectTokenType) {
            case ACCESS_TOKEN -> {
                val token = configurationContext.getTicketRegistry().getTicket(subjectToken, OAuth20AccessToken.class);
                val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), token.getClientId());
                yield new TokenExchangeRequest(token, token.getService(), registeredService, token.getAuthentication());
            }
            case JWT -> {
                val claimSet = configurationContext.getAccessTokenJwtBuilder().unpack(Optional.empty(), subjectToken);
                val service = configurationContext.getWebApplicationServiceServiceFactory().createService(claimSet.getIssuer());
                service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(claimSet.getIssuer()));
                val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), claimSet.getIssuer());
                val userProfile = extractUserProfile(webContext).orElseThrow();
                val authentication = configurationContext.getAuthenticationBuilder()
                    .build(userProfile, registeredService, webContext, service);
                yield new TokenExchangeRequest(claimSet, service, registeredService, authentication);
            }
            default -> throw new IllegalArgumentException("Subject token type %s is not supported".formatted(subjectTokenType));
        };
    }

    protected Authentication getActorTokenAuthentication(final WebContext webContext, final TokenExchangeRequest extractedRequest) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val actorToken = configurationContext.getRequestParameterResolver().resolveRequestParameter(webContext, OAuth20Constants.ACTOR_TOKEN);
        val actorTokenType = configurationContext.getRequestParameterResolver().resolveRequestParameter(webContext, OAuth20Constants.ACTOR_TOKEN_TYPE)
            .map(OAuth20TokenExchangeTypes::from);
        FunctionUtils.throwIf(actorToken.isPresent() && actorTokenType.isEmpty(),
            () -> new IllegalArgumentException("Actor token type cannot be undefined when actor token is provided"));
        if (actorToken.isPresent()) {
            val actorAuthentication = extractActorTokenAuthentication(webContext);
            val tokenExchangePolicy = extractedRequest.registeredService() != null ? extractedRequest.registeredService().getTokenExchangePolicy() : null;
            if (tokenExchangePolicy == null || tokenExchangePolicy.canSubjectTokenActAs(extractedRequest.authentication(),
                actorAuthentication, actorTokenType.map(OAuth20TokenExchangeTypes::getType).orElse(null))) {
                return actorAuthentication;
            }
        }
        return null;
    }

    protected Authentication extractActorTokenAuthentication(final WebContext webContext) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val actorToken = configurationContext.getRequestParameterResolver().resolveRequestParameter(webContext, OAuth20Constants.ACTOR_TOKEN).orElseThrow();
        val actorTokenType = configurationContext.getRequestParameterResolver().resolveRequestParameter(webContext, OAuth20Constants.ACTOR_TOKEN_TYPE)
            .map(OAuth20TokenExchangeTypes::from)
            .orElseThrow();
        return switch (actorTokenType) {
            case ACCESS_TOKEN -> {
                val token = configurationContext.getTicketRegistry().getTicket(actorToken, OAuth20Token.class);
                yield token.getAuthentication();
            }
            case JWT -> buildActorTokenAuthenticationFromJwt(actorToken, webContext);
            default -> throw new IllegalArgumentException("Actor token type %s is not supported".formatted(actorTokenType));
        };
    }

    protected Authentication buildActorTokenAuthenticationFromJwt(final String actorToken, final WebContext webContext) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val claimSet = configurationContext.getAccessTokenJwtBuilder().unpack(Optional.empty(), actorToken);
        val service = configurationContext.getWebApplicationServiceServiceFactory().createService(claimSet.getIssuer());
        service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(claimSet.getIssuer()));
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), claimSet.getIssuer());
        val userProfile = extractUserProfile(webContext).orElseThrow();
        return configurationContext.getAuthenticationBuilder().build(userProfile, registeredService, webContext, service);
    }

    public record TokenExchangeRequest(Serializable token, Service service,
        OAuthRegisteredService registeredService, Authentication authentication) {
    }
}
