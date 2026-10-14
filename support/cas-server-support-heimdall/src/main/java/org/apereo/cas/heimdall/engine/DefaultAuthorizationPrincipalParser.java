package org.apereo.cas.heimdall.engine;

import module java.base;
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
import org.apereo.cas.heimdall.authzen.AuthZenSubject;
import org.apereo.cas.heimdall.services.HeimdallRegisteredServiceAccessStrategy;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.validator.OAuth20ProofOfPossessionValidator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.context.WebContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import java.security.cert.X509Certificate;

/**
 * This is {@link DefaultAuthorizationPrincipalParser}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultAuthorizationPrincipalParser implements AuthorizationPrincipalParser {
    private static final String X509_CERTIFICATE_REQUEST_ATTRIBUTE = "jakarta.servlet.request.X509Certificate";

    private static final String RESOLVED_SUBJECT_TYPE = "user";

    protected final TicketRegistry ticketRegistry;
    protected final CasConfigurationProperties casProperties;
    protected final ObjectProvider<JwtBuilder> accessTokenJwtBuilder;
    protected final ObjectProvider<OAuth20TokenSigningAndEncryptionService> oidcTokenSigningAndEncryptionService;
    protected final AuthenticationSystemSupport authenticationSystemSupport;
    private final ObjectProvider<LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>>> oidcServiceJsonWebKeystoreCacheProvider;

    private final ObjectProvider<OAuth20ProofOfPossessionValidator> proofOfPossessionValidator;

    private final ObjectProvider<OAuth20ClientSecretValidator> clientSecretValidator;

    private final TicketFactory ticketFactory;

    @Override
    public Principal parse(final String authorizationHeader, final AuthorizationRequest authorizationRequest) throws Throwable {
        return parse(authorizationHeader, authorizationRequest, null);
    }

    @Override
    public @Nullable Principal parse(final String authorizationHeader, final AuthorizationRequest authorizationRequest,
                                     final @Nullable WebContext webContext)
            throws Throwable {
        val claims = parseAuthorizationHeader(authorizationHeader, authorizationRequest.isAuthZen(), webContext);
        val subject = authorizationRequest.getSubject();
        if (subject != null) {
            return resolveSubject(subject);
        }
        val principalAttributes = new HashMap(claims.getClaims());
        principalAttributes.put(HttpHeaders.AUTHORIZATION, authorizationHeader);
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(claims.getSubject(), principalAttributes);
    }

    @Override
    public void authenticateCaller(final String authorizationHeader, final @Nullable WebContext webContext) throws Throwable {
        parseAuthorizationHeader(authorizationHeader, true, webContext);
    }

    @Override
    public @Nullable Principal resolveSubject(final AuthZenSubject subject) throws Throwable {
        if (RESOLVED_SUBJECT_TYPE.equals(subject.getType())) {
            return authenticationSystemSupport.getPrincipalResolver().resolve(new BasicIdentifiableCredential(subject.getId()));
        }
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(subject.getId());
    }

    protected JWTClaimsSet parseAuthorizationHeader(final String authorizationHeader, final boolean authZen,
                                                    final @Nullable WebContext webContext)
            throws Throwable {
        if (Strings.CI.startsWith(authorizationHeader, "Basic ")) {
            val credentials = EncodingUtils.decodeBase64ToString(Strings.CI.removeStart(authorizationHeader, "Basic ").trim());
            FunctionUtils.throwIf(StringUtils.isBlank(credentials) || !credentials.contains(":"),
                () -> new AuthenticationException("Basic credentials must be formatted as id:secret"));
            val id = StringUtils.substringBefore(credentials, ":");
            val secret = StringUtils.substringAfter(credentials, ":");
            return authZen
                ? buildClaimSetFromClientCredentials(id, secret)
                : buildClaimSetFromAuthentication(id, secret);
        }
        val dpop = Strings.CI.startsWith(authorizationHeader, "DPoP ");
        if (dpop || Strings.CI.startsWith(authorizationHeader, "Bearer ")) {
            val token = authorizationHeader.substring(authorizationHeader.indexOf(' ') + 1).trim();
            val accessToken = resolveAccessToken(token);
            if (accessToken.isPresent()) {
                val ticket = accessToken.get();
                FunctionUtils.throwIf(ticket.isExpired(), () -> new AuthenticationException("Access token has expired"));
                validateClient(ticket.getClientId());
                val bound = ticket.getAuthentication().containsAttribute(OAuth20Constants.DPOP_CONFIRMATION);
                FunctionUtils.throwIf(dpop != bound, () -> new AuthenticationException("Invalid access token authorization scheme"));
                if (bound) {
                    FunctionUtils.throwIf(webContext == null, () -> new AuthenticationException("DPoP requires an HTTP request"));
                    proofOfPossessionValidator.getObject().validateProtectedResourceRequest(webContext, token, ticket);
                }
                val certificateBinding = findCertificateBinding(ticket);
                FunctionUtils.throwIf(certificateBinding.isPresent()
                        && (webContext == null || !isBoundClientCertificatePresented(webContext, certificateBinding.get())),
                    () -> new AuthenticationException("Access token is bound to a client certificate that was not presented"));
                val claims = buildClaimsFromAuthentication(ticket.getAuthentication());
                claims.putAll(ticket.getClaims());
                claims.put(OAuth20Constants.SCOPE, ticket.getScopes());
                return validateClaims(JWTClaimsSet.parse(claims));
            }
            FunctionUtils.throwIf(dpop, () -> new AuthenticationException("DPoP requires an access token"));
            val claims = parseOidcIdToken(token)
                .or(() -> parseJwtAuthorization(token))
                .orElseThrow(() -> new AuthenticationException("Unable to parse and verify token"));
            validateClient(claims.getStringClaim(OAuth20Constants.CLIENT_ID));
            FunctionUtils.throwIf(claims.getClaim("cnf") != null || claims.getClaim(OAuth20Constants.DPOP_CONFIRMATION) != null,
                () -> new AuthenticationException("Sender-constrained tokens require access token validation"));
            return validateClaims(claims);
        }
        throw new AuthenticationException("Unknown authorization header type");
    }

    protected void validateClient(final String clientId) throws Throwable {
        FunctionUtils.throwIf(StringUtils.isBlank(clientId), () -> new AuthenticationException("Client id is missing"));
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            accessTokenJwtBuilder.getObject().getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        val accessStrategy = determineHeimdallAccessStrategy(registeredService);
        FunctionUtils.throwIf(accessStrategy != null && !accessStrategy.isAllowed(),
            () -> new AuthenticationException("Heimdall access strategy is not allowed for service " + registeredService.getName()));
    }

    protected Optional<String> findCertificateBinding(final OAuth20AccessToken accessToken) {
        val authentication = accessToken.getAuthentication();
        return Stream.of(authentication.getAttributes(), authentication.getPrincipal().getAttributes())
            .map(attributes -> attributes.get(OAuth20Constants.X509_CERTIFICATE_DIGEST))
            .filter(Objects::nonNull)
            .map(CollectionUtils::firstElement)
            .flatMap(Optional::stream)
            .map(Object::toString)
            .findFirst();
    }

    protected boolean isBoundClientCertificatePresented(final WebContext webContext, final String certificateDigest) {
        return webContext.getRequestAttribute(X509_CERTIFICATE_REQUEST_ATTRIBUTE, X509Certificate[].class)
            .filter(certificates -> certificates.length > 0)
            .map(certificates -> OAuth20Utils.computeCertificateThumbprint(certificates[0]))
            .filter(digest -> MessageDigest.isEqual(digest.getBytes(StandardCharsets.UTF_8), certificateDigest.getBytes(StandardCharsets.UTF_8)))
            .isPresent();
    }

    private static @Nullable HeimdallRegisteredServiceAccessStrategy determineHeimdallAccessStrategy(
        final RegisteredService registeredService) {
        val accessStrategy = registeredService.getAccessStrategy();
        if (accessStrategy instanceof final ChainingRegisteredServiceAccessStrategy chain) {
            return chain.getStrategies()
                .stream()
                .filter(HeimdallRegisteredServiceAccessStrategy.class::isInstance)
                .map(HeimdallRegisteredServiceAccessStrategy.class::cast)
                .findFirst()
                .orElse(null);
        }
        if (accessStrategy instanceof final HeimdallRegisteredServiceAccessStrategy heimdallAccessStrategy) {
            return heimdallAccessStrategy;
        }
        return null;
    }

    protected Optional<OAuth20AccessToken> resolveAccessToken(final String token) {
        if (token.startsWith(OAuth20AccessToken.PREFIX + '-')) {
            return Optional.ofNullable(ticketRegistry.getTicket(token, OAuth20AccessToken.class));
        }
        try {
            return accessTokenJwtBuilder.stream()
                .map(builder -> OAuth20JwtAccessTokenEncoder.toDecodableCipher(builder).decode(token))
                .filter(Objects::nonNull)
                .map(id -> ticketRegistry.getTicket(id, OAuth20AccessToken.class))
                .filter(Objects::nonNull)
                .findFirst();
        } catch (final Exception e) {
            LOGGER.debug("Unable to resolve JWT access token", LOGGER.isTraceEnabled() ? e : null);
            return Optional.empty();
        }
    }

    protected Optional<JWTClaimsSet> parseJwtAuthorization(final String token) {
        try {
            val clientIdInAssertion = OAuth20Utils.extractClientIdFromToken(token);
            validateClient(clientIdInAssertion);
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
                Set.of(OidcConstants.ISS, OidcConstants.AUD, OAuth20Constants.CLAIM_SUB, OAuth20Constants.CLAIM_EXP,
                    JWTClaimNames.JWT_ID, JWTClaimNames.ISSUED_AT),
                Set.of());
            val claimSet = JWTClaimsSet.parse(claims.getClaimsMap());
            jwtClaimsSetVerifier.verify(claimSet, new SimpleSecurityContext());
            validateAssertionLifetime(claimSet);
            consumeAssertion(registeredService.getClientId(), claimSet);
            return Optional.of(claimSet);
        } catch (final Throwable e) {
            LOGGER.debug(e.getMessage(), LOGGER.isTraceEnabled() ? e : null);
            return Optional.empty();
        }
    }

    protected void validateAssertionLifetime(final JWTClaimsSet claimSet) throws Throwable {
        val maxClockSkew = Beans.newDuration(casProperties.getAuthn().getOidc().getCore().getSkew());
        val maxLifetime = Beans.newDuration(casProperties.getHeimdall().getJwtAssertionMaxLifetime());
        val issuedAt = claimSet.getIssueTime().toInstant();
        val lifetime = Duration.between(issuedAt, claimSet.getExpirationTime().toInstant());
        FunctionUtils.throwIf(issuedAt.isAfter(Instant.now(Clock.systemUTC()).plus(maxClockSkew)),
            () -> new AuthenticationException("JWT assertion is issued in the future"));
        FunctionUtils.throwIf(lifetime.isNegative() || lifetime.compareTo(maxLifetime) > 0,
            () -> new AuthenticationException("JWT assertion lifetime %s exceeds %s".formatted(lifetime, maxLifetime)));
    }

    /**
     * Record the assertion's {@code jti} until the assertion expires, so it can be presented only once.
     * This mirrors the DPoP proof replay check: a lookup followed by a registry write.
     *
     * @param clientId the client that signed the assertion
     * @param claimSet the verified assertion claims
     * @throws Throwable if the assertion was already used or cannot be recorded
     */
    protected void consumeAssertion(final String clientId, final JWTClaimsSet claimSet) throws Throwable {
        val key = "heimdall-assertion:" + clientId + ':' + DigestUtils.sha256(claimSet.getJWTID());
        FunctionUtils.throwIf(isAssertionRecorded(TransientSessionTicketFactory.normalizeTicketId(key)),
            () -> new AuthenticationException("JWT assertion has already been used"));
        val maxClockSkew = Beans.newDuration(casProperties.getAuthn().getOidc().getCore().getSkew());
        val timeToLive = Duration.between(Instant.now(Clock.systemUTC()), claimSet.getExpirationTime().toInstant()).plus(maxClockSkew);
        val expirationPolicy = new HardTimeoutExpirationPolicy(Math.max(1, timeToLive.toSeconds()));
        val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        val properties = new HashMap<String, Serializable>();
        properties.put(OAuth20Constants.CLIENT_ID, clientId);
        properties.put(ExpirationPolicy.class.getName(), expirationPolicy);
        val ticket = factory.create(key, properties);
        ticketRegistry.addTicket(ticket);
    }

    private boolean isAssertionRecorded(final String ticketId) {
        try {
            return ticketRegistry.getTicket(ticketId, TransientSessionTicket.class) != null;
        } catch (final InvalidTicketException e) {
            return false;
        }
    }

    protected JWTClaimsSet buildClaimSetFromAuthentication(final String username, final String password) throws Throwable {
        val credential = new UsernamePasswordCredential(username, password);
        val authResultBuilder = authenticationSystemSupport.handleInitialAuthenticationTransaction(null, credential);
        val authentication = authenticationSystemSupport.finalizeAllAuthenticationTransactions(authResultBuilder, null);
        val claimsMap = buildClaimsFromAuthentication(authentication.getAuthentication());
        return JWTClaimsSet.parse(claimsMap);
    }

    protected JWTClaimsSet buildClaimSetFromClientCredentials(final String clientId, final String clientSecret) throws Throwable {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            accessTokenJwtBuilder.getObject().getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        FunctionUtils.throwIf(StringUtils.isBlank(clientSecret)
                || registeredService.getClientSecrets() == null || registeredService.getClientSecrets().isEmpty()
                || !clientSecretValidator.getObject().validate(registeredService, clientSecret),
            () -> new AuthenticationException("Invalid client credentials"));
        validateClient(clientId);
        return JWTClaimsSet.parse(Map.of(OAuth20Constants.CLAIM_SUB, clientId, OAuth20Constants.CLIENT_ID, clientId));
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

    protected Map<String, Object> buildClaimsFromAuthentication(final Authentication authentication) {
        val claimsMap = new HashMap<String, Object>();
        claimsMap.putAll(authentication.getAttributes());
        claimsMap.putAll(authentication.getPrincipal().getAttributes());
        claimsMap.put(OAuth20Constants.CLAIM_SUB, authentication.getPrincipal().getId());
        return claimsMap;
    }

    protected Optional<JWTClaimsSet> parseOidcIdToken(final String token) {
        try {
            val jwt = JWTParser.parse(token);
            val type = jwt.getHeader().getType();
            val id = jwt.getJWTClaimsSet().getJWTID();
            if ((type != null && !"JWT".equals(type.toString()))
                || (id != null && id.startsWith(OAuth20AccessToken.PREFIX + '-'))) {
                return Optional.empty();
            }
            return oidcTokenSigningAndEncryptionService
                .stream()
                .map(service -> service.decode(token, Optional.empty()))
                .map(Unchecked.function(claims -> JWTClaimsSet.parse(claims.getClaimsMap())))
                .filter(Unchecked.predicate(claims -> claims.getExpirationTime() != null
                    && claims.getAudience().contains(claims.getStringClaim(OAuth20Constants.CLIENT_ID))))
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
