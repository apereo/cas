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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
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
public class AccessTokenTokenExchangeGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor {
    public AccessTokenTokenExchangeGrantRequestExtractor(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public boolean supports(final WebContext context) {
        val grantType = getConfigurationContext().getRequestParameterResolver()
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
        val requestParameterResolver = getConfigurationContext().getRequestParameterResolver();
        val scopes = requestParameterResolver.resolveRequestScopes(webContext);

        val requestedTokenType = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.REQUESTED_TOKEN_TYPE)
            .orElseGet(OAuth20TokenExchangeTypes.ACCESS_TOKEN::getType);
        val subjectTokenType = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN_TYPE)
            .map(OAuth20TokenExchangeTypes::from)
            .orElseThrow(() -> new IllegalArgumentException("Subject token type cannot be undefined"));
        val subjectToken = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN)
            .orElseThrow(() -> new IllegalArgumentException("Subject token cannot be undefined"));
        val resource = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.RESOURCE);
        val audience = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.AUDIENCE).orElse(null);

        val extractedRequest = switch (subjectTokenType) {
            case ACCESS_TOKEN -> {
                val token = getConfigurationContext().getTicketRegistry().getTicket(subjectToken, OAuth20Token.class);
                val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), token.getClientId());
                yield new ExtractedRequest(token, token.getService(), registeredService, token.getAuthentication());
            }
            case JWT -> {
                val claimSet = getConfigurationContext().getAccessTokenJwtBuilder().unpack(Optional.empty(), subjectToken);
                val service = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(claimSet.getIssuer());
                service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(claimSet.getIssuer()));
                val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), claimSet.getIssuer());
                val authentication = getConfigurationContext().getAuthenticationBuilder()
                    .build(extractUserProfile(webContext).orElseThrow(), registeredService, webContext, service);
                yield new ExtractedRequest(claimSet, service, registeredService, authentication);
            }
        };

        val resourceService = resource
            .map(res -> {
                val service = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(res);
                service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(extractedRequest.registeredService().getClientId()));
                return service;
            }).orElse(null);

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
            .build();
    }

    private record ExtractedRequest(Serializable token, Service service,
        OAuthRegisteredService registeredService, Authentication authentication) {
    }
}
