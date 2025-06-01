package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OAuth20TokenExchangeGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class OAuth20TokenExchangeGrantTypeTokenRequestValidator<T extends OAuth20ConfigurationContext> extends BaseOAuth20TokenRequestValidator<T> {
    private static final Set<String> JWT_TOKEN_TYPE_REQUIRED_CLAIMS = Set.of("iss", "aud", "sub", "exp", "iat", "nbf");
    private final JWTClaimsSetVerifier jwtClaimsSetVerifier;

    public OAuth20TokenExchangeGrantTypeTokenRequestValidator(final ObjectProvider<T> configurationContext) {
        super(configurationContext);
        jwtClaimsSetVerifier = new DefaultJWTClaimsVerifier<>(null, JWT_TOKEN_TYPE_REQUIRED_CLAIMS);
    }

    @Override
    protected boolean validateInternal(final WebContext webContext, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val requestParameterResolver = configurationContext.getRequestParameterResolver();
        val subjectTokenType = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN_TYPE)
            .orElseThrow(() -> new IllegalArgumentException("Subject token type cannot be undefined"));
        val subjectToken = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN)
            .orElseThrow(() -> new IllegalArgumentException("Subject token cannot be undefined"));
        val requestedTokenType = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.REQUESTED_TOKEN_TYPE)
            .orElseGet(OAuth20TokenExchangeTypes.ACCESS_TOKEN::getType);

        val registeredService = extractRegisteredService(subjectTokenType, subjectToken);

        val audit = AuditableContext.builder().registeredService(registeredService).build();
        val accessResult = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        if (!isGrantTypeSupportedBy(registeredService, getGrantType().getType(), true)) {
            LOGGER.warn("Requested grant type [{}] is not authorized by service definition [{}]",
                grantType, Objects.requireNonNull(registeredService).getServiceId());
            return false;
        }

        val actorToken = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.ACTOR_TOKEN);
        val actorTokenType = requestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.ACTOR_TOKEN_TYPE).map(OAuth20TokenExchangeTypes::from);
        FunctionUtils.throwIf(actorToken.isPresent() && actorTokenType.isEmpty(),
            () -> new IllegalArgumentException("Actor token type cannot be undefined when actor token is provided"));

        val resourceAndAudience = requestParameterResolver.resolveRequestParameters(
            List.of(OAuth20Constants.RESOURCE, OAuth20Constants.AUDIENCE), webContext);
        val resources = resourceAndAudience.getOrDefault(OAuth20Constants.RESOURCE, Set.of());
        val audience = resourceAndAudience.getOrDefault(OAuth20Constants.AUDIENCE, Set.of());
        val tokenExchangePolicy = registeredService.getTokenExchangePolicy();
        return tokenExchangePolicy == null || tokenExchangePolicy.isTokenExchangeAllowed(registeredService, resources, audience, requestedTokenType);
    }

    protected OAuthRegisteredService extractRegisteredService(final String subjectTokenType,
                                                              final String subjectToken) throws Exception {
        val configurationContext = getConfigurationContext().getObject();
        return switch (OAuth20TokenExchangeTypes.from(subjectTokenType)) {
            case ACCESS_TOKEN -> {
                val accessToken = configurationContext.getTicketRegistry().getTicket(subjectToken, OAuth20AccessToken.class);
                yield OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), accessToken.getClientId());
            }
            case JWT -> {
                val claimSet = configurationContext.getAccessTokenJwtBuilder().unpack(Optional.empty(), subjectToken);
                jwtClaimsSetVerifier.verify(claimSet, new SimpleSecurityContext());
                val service = configurationContext.getWebApplicationServiceServiceFactory().createService(claimSet.getIssuer());
                service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(claimSet.getSubject()));
                yield OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), claimSet.getSubject());
            }
            default -> throw new IllegalArgumentException("Subject token type %s is not supported".formatted(subjectTokenType));
        };
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.TOKEN_EXCHANGE;
    }
}
