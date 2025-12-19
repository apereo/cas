package org.apereo.cas.oidc.token;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jspecify.annotations.NonNull;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link OidcAccessTokenJwtBearerGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
public class OidcAccessTokenJwtBearerGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor<OidcConfigurationContext> {
    private final ObjectProvider<@NonNull LoadingCache<@NonNull OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>>> oidcServiceJsonWebKeystoreCacheProvider;

    public OidcAccessTokenJwtBearerGrantRequestExtractor(
        final ObjectProvider<@NonNull OidcConfigurationContext> config,
        final ObjectProvider<@NonNull LoadingCache<@NonNull OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>>> oidcServiceJsonWebKeystoreCache) {
        super(config);
        this.oidcServiceJsonWebKeystoreCacheProvider = oidcServiceJsonWebKeystoreCache;
    }

    @Override
    protected AccessTokenRequestContext extractRequest(final WebContext webContext) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val grantType = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(webContext, OAuth20Constants.GRANT_TYPE);
        LOGGER.debug("OAuth grant type is [{}]", grantType);
        val assertion = getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(webContext, OAuth20Constants.ASSERTION).orElseThrow();
        val clientIdInAssertion = OAuth20Utils.extractClientIdFromToken(assertion);
        LOGGER.debug("Client id retrieved from ID token is [{}]", clientIdInAssertion);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(), clientIdInAssertion, OidcRegisteredService.class);
        LOGGER.debug("Located registered service [{}]", registeredService);
        val service = verifyServiceAccess(registeredService);
        val claims = extractAssertionClaims(registeredService, assertion);
        val generateRefreshToken = isAllowedToGenerateRefreshToken() && registeredService.isGenerateRefreshToken();
        val requestedScopes = configurationContext.getRequestParameterResolver().resolveRequestScopes(webContext);
        LOGGER.debug("Requested scopes are [{}]", requestedScopes);

        val profile = createAuthenticationProfile(claims, requestedScopes, registeredService);
        val authentication = configurationContext.getAuthenticationBuilder()
            .build(profile, registeredService, webContext, service);

        val builder = AccessTokenRequestContext
            .builder()
            .scopes(requestedScopes)
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .grantType(getGrantType())
            .generateRefreshToken(generateRefreshToken);
        return extractInternal(webContext, builder.build());
    }

    protected CommonProfile createAuthenticationProfile(final JwtClaims claims, final Set<String> requestedScopes,
                                                        final OAuthRegisteredService registeredService) throws Throwable {
        val profile = new CommonProfile();
        profile.setId(claims.getSubject());
        profile.addRoles(requestedScopes);
        profile.addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId());

        val attributes = new LinkedHashMap<>(claims.getClaimsMap());
        val resolvedPrincipal = getConfigurationContext().getObject().getPrincipalResolver()
            .resolve(new BasicIdentifiableCredential(claims.getSubject()));
        attributes.putAll(resolvedPrincipal.getAttributes());

        profile.addAttributes(attributes);
        return profile;
    }

    protected Service verifyServiceAccess(final OAuthRegisteredService registeredService) throws Throwable {
        val audit = AuditableContext.builder().registeredService(registeredService).build();
        val accessResult = getConfigurationContext().getObject().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();
        val service = getConfigurationContext().getObject()
            .getWebApplicationServiceServiceFactory()
            .createService(registeredService.getClientId());
        service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(registeredService.getClientId()));
        return service;
    }

    protected JwtClaims extractAssertionClaims(final OidcRegisteredService registeredService, final String assertion) throws Exception {
        val jsonWebKeys = getJsonWebKeyToVerifyAssertion(registeredService);
        val verifiedAssertion = verifyAssertion(assertion, jsonWebKeys);
        val claims = JwtClaims.parse(verifiedAssertion);

        val baseOidcUrl = getConfigurationContext().getObject().getCasProperties().getServer().getPrefix() + '/' + OidcConstants.BASE_OIDC_URL + '/';
        val jwtClaimsSetVerifier = new DefaultJWTClaimsVerifier<>(
            CollectionUtils.wrapSet(
                baseOidcUrl + OAuth20Constants.ACCESS_TOKEN_URL,
                baseOidcUrl + OAuth20Constants.TOKEN_URL,
                baseOidcUrl + OidcConstants.ACCESS_TOKEN_URL,
                baseOidcUrl + OidcConstants.TOKEN_URL),
            JWTClaimsSet.parse(Map.of(OidcConstants.ISS, registeredService.getClientId())),
            Set.of(OidcConstants.ISS, OidcConstants.AUD, OAuth20Constants.CLAIM_SUB, OAuth20Constants.CLAIM_EXP),
            Set.of());
        jwtClaimsSetVerifier.verify(JWTClaimsSet.parse(claims.getClaimsMap()), new SimpleSecurityContext());
        return claims;
    }

    protected String verifyAssertion(final String assertion, final List<PublicJsonWebKey> jsonWebKeys) {
        for (val jsonWebKey : jsonWebKeys) {
            try {
                val verified = EncodingUtils.verifyJwsSignature(jsonWebKey.getPublicKey(), assertion);
                val verifiedAssertion = new String(verified, StandardCharsets.UTF_8);
                LOGGER.trace("Successfully verified JWT assertion with key id [{}]", jsonWebKey.getKeyId());
                return verifiedAssertion;
            } catch (final Exception e) {
                LOGGER.debug("Failed to verify JWT assertion via key id [{}]: [{}]. Moving on to the next key",
                    jsonWebKey.getKeyId(), e.getMessage());
            }
        }
        throw new IllegalArgumentException("Unable to verify JWT assertion with any of the configured JSON web keys");
    }

    protected AccessTokenRequestContext extractInternal(
        final WebContext context,
        final AccessTokenRequestContext tokenRequestContext) {
        return tokenRequestContext;
    }

    protected static boolean isAllowedToGenerateRefreshToken() {
        return true;
    }

    @Override
    public boolean supports(final WebContext context) {
        val grantType = getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
        val assertion = getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.ASSERTION).orElse(StringUtils.EMPTY);
        return StringUtils.isNotBlank(assertion) && OAuth20Utils.isGrantType(grantType, getGrantType());
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.JWT_BEARER;
    }

    @Override
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.NONE;
    }

    protected List<PublicJsonWebKey> getJsonWebKeyToVerifyAssertion(final OAuthRegisteredService registeredService) {
        val result = oidcServiceJsonWebKeystoreCacheProvider.getObject()
            .get(new OidcJsonWebKeyCacheKey(registeredService, OidcJsonWebKeyUsage.SIGNING));
        return Objects.requireNonNull(result)
            .stream()
            .map(JsonWebKeySet::getJsonWebKeys)
            .flatMap(List::stream)
            .filter(PublicJsonWebKey.class::isInstance)
            .filter(key -> key.getKey() != null)
            .map(PublicJsonWebKey.class::cast)
            .toList();
    }
}
