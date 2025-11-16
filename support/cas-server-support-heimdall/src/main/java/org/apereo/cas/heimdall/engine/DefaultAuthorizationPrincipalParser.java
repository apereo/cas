package org.apereo.cas.heimdall.engine;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.base.Splitter;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link DefaultAuthorizationPrincipalParser}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultAuthorizationPrincipalParser implements AuthorizationPrincipalParser {
    protected final TicketRegistry ticketRegistry;
    protected final CasConfigurationProperties casProperties;
    protected final ObjectProvider<@NonNull JwtBuilder> accessTokenJwtBuilder;
    protected final ObjectProvider<@NonNull OAuth20TokenSigningAndEncryptionService> oidcTokenSigningAndEncryptionService;
    protected final AuthenticationSystemSupport authenticationSystemSupport;
    private final ObjectProvider<@NonNull LoadingCache<@NonNull OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>>> oidcServiceJsonWebKeystoreCacheProvider;

    @Override
    public Principal parse(final String authorizationHeader, final AuthorizationRequest authorizationRequest) throws Throwable {
        val claims = parseAuthorizationHeader(authorizationHeader);
        val principalAttributes = new HashMap(claims.getClaims());
        principalAttributes.put(HttpHeaders.AUTHORIZATION, authorizationHeader);
        if (authorizationRequest.getSubject() != null) {
            val credential = new BasicIdentifiableCredential(authorizationRequest.getSubject().getId());
            return authenticationSystemSupport.getPrincipalResolver().resolve(credential);
        }
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(claims.getSubject(), principalAttributes);
    }

    protected JWTClaimsSet parseAuthorizationHeader(final String authorizationHeader) throws Throwable {
        if (authorizationHeader.startsWith("Basic ")) {
            val token = Strings.CI.removeStart(authorizationHeader, "Basic ");
            return buildClaimSetFromAuthentication(token);
        }
        if (authorizationHeader.startsWith("Bearer ")) {
            val token = Strings.CI.removeStart(authorizationHeader, "Bearer ");
            val claims = parseOidcIdToken(token)
                .or(() -> parseJwtAccessToken(token))
                .or(() -> getJwtClaimsSetFromAccessToken(token))
                .or(() -> parseJwtAuthorization(token))
                .orElseThrow(() -> new AuthenticationException("Unable to parse and verify token"));
            return validateClaims(claims);
        }
        throw new AuthenticationException("Unknown authorization header type");
    }

    protected Optional<JWTClaimsSet> parseJwtAuthorization(final String token){
        try {
            val clientIdInAssertion = OAuth20Utils.extractClientIdFromToken(token);
            LOGGER.debug("Client id retrieved from ID token is [{}]", clientIdInAssertion);
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                accessTokenJwtBuilder.getObject().getServicesManager(),
                clientIdInAssertion, OidcRegisteredService.class);

            val jsonWebKeys = getJsonWebKeyToVerifyAssertion(registeredService);
            val verifiedAssertion = verifyAssertion(token, jsonWebKeys);
            val claims = JwtClaims.parse(verifiedAssertion);

            val baseOidcUrl = accessTokenJwtBuilder.getObject().getCasProperties()
                .getServer().getPrefix() + '/' + OidcConstants.BASE_OIDC_URL + '/';
            val jwtClaimsSetVerifier = new DefaultJWTClaimsVerifier<>(
                CollectionUtils.wrapSet(
                    baseOidcUrl + OAuth20Constants.ACCESS_TOKEN_URL,
                    baseOidcUrl + OAuth20Constants.TOKEN_URL,
                    baseOidcUrl + OidcConstants.ACCESS_TOKEN_URL,
                    baseOidcUrl + OidcConstants.TOKEN_URL),
                JWTClaimsSet.parse(Map.of(OidcConstants.ISS, registeredService.getClientId())),
                Set.of(OidcConstants.ISS, OidcConstants.AUD, OAuth20Constants.CLAIM_SUB, OAuth20Constants.CLAIM_EXP),
                Set.of());
            val claimSet = JWTClaimsSet.parse(claims.getClaimsMap());
            jwtClaimsSetVerifier.verify(claimSet, new SimpleSecurityContext());
            return Optional.of(claimSet);
        } catch (final Throwable e) {
            LOGGER.debug(e.getMessage(), LOGGER.isTraceEnabled() ? e : null);
            return Optional.empty();
        }
    }

    protected JWTClaimsSet buildClaimSetFromAuthentication(final String token) throws Throwable {
        val usernamePass = Splitter.on(':').splitToList(EncodingUtils.decodeBase64ToString(token));
        val credential = new UsernamePasswordCredential(usernamePass.getFirst(), usernamePass.getLast());
        val authResultBuilder = authenticationSystemSupport.handleInitialAuthenticationTransaction(null, credential);
        val authentication = authenticationSystemSupport.finalizeAllAuthenticationTransactions(authResultBuilder, null);
        val claimsMap = buildClaimsFromAuthentication(authentication.getAuthentication());
        return JWTClaimsSet.parse(claimsMap);
    }

    protected JWTClaimsSet validateClaims(final JWTClaimsSet claimsSet) {
        val maxClockSkew = Beans.newDuration(casProperties.getAuthn().getOidc().getCore().getSkew()).toSeconds();
        val now = new Date();
        val exp = claimsSet.getExpirationTime();
        if (exp != null && !DateUtils.isAfter(exp, now, maxClockSkew)) {
            throw new AuthenticationException("Token has expired: %s and is after %s".formatted(exp, now));
        }
        val nbf = claimsSet.getNotBeforeTime();
        if (nbf != null && !DateUtils.isBefore(nbf, now, maxClockSkew)) {
            throw new AuthenticationException("Token cannot be used before %s and now is %s".formatted(nbf, now));
        }
        return claimsSet;
    }

    private Optional<JWTClaimsSet> getJwtClaimsSetFromAccessToken(final String token) {
        try {
            val ticket = ticketRegistry.getTicket(token, OAuth20AccessToken.class);
            FunctionUtils.throwIf(ticket == null || ticket.isExpired(),
                () -> new AuthenticationException("Token %s is not found or has expired".formatted(token)));
            val claimsMap = buildClaimsFromAuthentication(ticket.getAuthentication());
            claimsMap.putAll(ticket.getClaims());
            claimsMap.put(OAuth20Constants.SCOPE, ticket.getScopes());
            claimsMap.put(OAuth20Constants.TOKEN, token);
            return Optional.of(JWTClaimsSet.parse(claimsMap));
        } catch (final Throwable e) {
            LOGGER.debug(e.getMessage(), LOGGER.isTraceEnabled() ? e : null);
            return Optional.empty();
        }
    }

    protected Map<String, Object> buildClaimsFromAuthentication(final Authentication authentication) {
        val claimsMap = new HashMap<String, Object>();
        claimsMap.putAll(authentication.getAttributes());
        claimsMap.putAll(authentication.getPrincipal().getAttributes());
        claimsMap.put(OAuth20Constants.CLAIM_SUB, authentication.getPrincipal().getId());
        return claimsMap;
    }
    
    protected Optional<JWTClaimsSet> parseJwtAccessToken(final String token) {
        try {
            return accessTokenJwtBuilder
                .stream()
                .map(builder -> {
                    val decodableCipher = OAuth20JwtAccessTokenEncoder.toDecodableCipher(builder);
                    val accessTokenId = decodableCipher.decode(token);
                    return getJwtClaimsSetFromAccessToken(accessTokenId);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), LOGGER.isTraceEnabled() ? e : null);
            return Optional.empty();
        }
    }

    protected Optional<JWTClaimsSet> parseOidcIdToken(final String token) {
        try {
            return oidcTokenSigningAndEncryptionService
                .stream()
                .map(service -> service.decode(token, Optional.empty()))
                .map(Unchecked.function(claims -> JWTClaimsSet.parse(claims.getClaimsMap())))
                .findFirst();
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), LOGGER.isTraceEnabled() ? e : null);
            return Optional.empty();
        }
    }

    protected List<PublicJsonWebKey> getJsonWebKeyToVerifyAssertion(final OAuthRegisteredService registeredService) {
        return oidcServiceJsonWebKeystoreCacheProvider
            .stream()
            .map(provider -> provider.get(new OidcJsonWebKeyCacheKey(registeredService, OidcJsonWebKeyUsage.SIGNING)))
            .filter(Objects::nonNull)
            .flatMap(Optional::stream)
            .map(JsonWebKeySet::getJsonWebKeys)
            .flatMap(List::stream)
            .filter(PublicJsonWebKey.class::isInstance)
            .filter(key -> key.getKey() != null)
            .map(PublicJsonWebKey.class::cast)
            .toList();
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
}
