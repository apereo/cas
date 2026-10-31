package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationEntry;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationStore;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationToken;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientCredentialsAuthenticator;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcJwtAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcJwtAuthenticator implements OAuth20ClientCredentialsAuthenticator {
    protected final OidcIssuerService issuerService;

    protected final ServicesManager servicesManager;

    protected final AuditableExecution registeredServiceAccessStrategyEnforcer;

    protected final TicketRegistry ticketRegistry;

    protected final TicketFactory ticketFactory;

    protected final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    protected final CasConfigurationProperties casProperties;

    protected final ApplicationContext applicationContext;

    protected final OidcServerDiscoverySettings oidcServerDiscoverySettings;

    protected final ObjectProvider<ClientJwksRegistrationStore> clientJwksRegistrationStore;

    protected final CipherExecutor<Serializable, String> registeredServiceCipherExecutor;

    protected @Nullable JWT verifyCredentials(final OAuth20ClientAuthenticationToken credentials) {
        if (!Strings.CI.equals(OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER, credentials.getUsername())) {
            LOGGER.debug("client assertion type is not set to [{}]", OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER);
            return null;
        }
        val assertion = (String) credentials.getCredentials();
        if (StringUtils.isBlank(assertion)) {
            LOGGER.debug("No assertion is available in the provided credentials");
            return null;
        }

        try {
            val jwt = JWTParser.parse(assertion);
            val alg = jwt.getHeader().getAlgorithm();
            if (!validateJwtAlgorithm(alg)) {
                LOGGER.debug("No assertion is available in the provided credentials");
                return null;
            }
            return jwt;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }


    @Override
    public Optional<OAuth20UserProfile> authenticate(final OAuth20ClientAuthenticationToken credentials) {
        return FunctionUtils.doAndHandle(() -> {
            val request = credentials.getRequest();
            val registeredService = getOidcRegisteredService(request);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
            Objects.requireNonNull(registeredService, "regisetered service is null");

            if (OAuth20Utils.isClientAuthenticationRequest(request)) {
                val authMethodDisabled = oidcServerDiscoverySettings.getTokenEndpointAuthMethodsSupported()
                    .stream()
                    .map(OAuth20ClientAuthenticationMethods::parse)
                    .noneMatch(method -> method == OAuth20ClientAuthenticationMethods.CLIENT_SECRET_JWT || method == OAuth20ClientAuthenticationMethods.PRIVATE_KEY_JWT);
                if (authMethodDisabled || !OAuth20Utils.isTokenAuthenticationMethodSupportedFor(request, registeredService,
                    OAuth20ClientAuthenticationMethods.CLIENT_SECRET_JWT, OAuth20ClientAuthenticationMethods.PRIVATE_KEY_JWT)) {
                    LOGGER.warn("Private key JWT authentication method is not enabled for CAS, or is not supported for service [{}]", registeredService.getName());
                    return Optional.<OAuth20UserProfile>empty();
                }
            }

            val jwt = verifyCredentials(credentials);
            if (jwt == null) {
                LOGGER.warn("Unable to verify credentials");
                return Optional.<OAuth20UserProfile>empty();
            }

            val keys = new JsonWebKeySet();
            clientJwksRegistrationStore.ifAvailable(Unchecked.consumer(store -> {
                if (jwt instanceof final SignedJWT signedJWT) {
                    val jwk = signedJWT.getHeader().getJWK();
                    val kid = signedJWT.getHeader().getKeyID();
                    val jkt = jwk != null ? jwk.computeThumbprint().toString() : StringUtils.EMPTY;
                    store.findBy(registeredService.getClientId(), jkt)
                        .or(() -> store.findBy(registeredService.getClientId(), kid))
                        .map(ClientJwksRegistrationEntry::jwk)
                        .ifPresent(registereredKey -> {
                            val webKey = EncodingUtils.newJsonWebKey(registereredKey);
                            keys.addJsonWebKey(webKey);
                        });
                }
            }));

            val authenticationMethod = registeredService.getTokenEndpointAuthenticationMethod();
            if (StringUtils.isBlank(authenticationMethod)
                || Strings.CI.equals(authenticationMethod, OAuth20ClientAuthenticationMethods.CLIENT_SECRET_JWT.getType())) {
                registeredService.getClientSecrets()
                    .stream()
                    .filter(secret -> !secret.hasClientSecretExpired(registeredService))
                    .map(secret -> SpringExpressionLanguageValueResolver.getInstance().resolve(secret.getValue()))
                    .map(secret -> registeredServiceCipherExecutor.decode(secret, new Object[]{registeredService}))
                    .filter(StringUtils::isNotBlank)
                    .map(secret -> new OctetSequenceJsonWebKey(new HmacKey(secret.getBytes(StandardCharsets.UTF_8))))
                    .forEach(keys::addJsonWebKey);
            }

            OidcJsonWebKeyStoreUtils.getJsonWebKeySet(registeredService,
                    applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING))
                .ifPresent(set -> set.getJsonWebKeys().forEach(keys::addJsonWebKey));

            val baseUrl = casProperties.getServer().getPrefix().concat('/' + OidcConstants.BASE_OIDC_URL + '/');
            val audience = new String[]{
                baseUrl + OidcConstants.ACCESS_TOKEN_URL,
                baseUrl + OAuth20Constants.ACCESS_TOKEN_URL,
                baseUrl + OAuth20Constants.TOKEN_URL
            };
            val verificationKeys = keys.getJsonWebKeys()
                .stream()
                .filter(jsonWebKey -> isVerificationKeyUsable(jwt, jsonWebKey, registeredService))
                .toList();
            val assertion = Objects.requireNonNull((String) credentials.getCredentials());
            val profile = verificationKeys
                .stream()
                .map(jsonWebKey -> new JwtConsumerBuilder()
                    .setVerificationKey(jsonWebKey.getKey())
                    .setRequireJwtId()
                    .setRequireExpirationTime()
                    .setRequireSubject()
                    .setExpectedIssuer(true, registeredService.getClientId())
                    .setExpectedSubject(registeredService.getClientId())
                    .setExpectedAudience(true, audience)
                    .build())
                .map(consumer -> determineUserProfile(assertion, consumer))
                .filter(Objects::nonNull)
                .findFirst();
            if (profile.isPresent() && !registerJwtIdentifier(jwt, registeredService.getClientId())) {
                LOGGER.warn("Client assertion JWT identifier has already been used");
                return Optional.<OAuth20UserProfile>empty();
            }
            profile.ifPresent(found -> found.addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId()));
            return profile;
        }, e -> {
            LoggingUtils.error(LOGGER, e);
            return Optional.<OAuth20UserProfile>empty();
        }).get();
    }

    protected @Nullable OidcRegisteredService getOidcRegisteredService(final HttpServletRequest request) throws Throwable {
        val code = Objects.requireNonNullElse(request.getParameter(OAuth20Constants.CODE), StringUtils.EMPTY);
        val givenCode = OAuth20Utils.getRequestedOAuthToken(request, ticketRegistry, code, OAuth20Code.class);
        if (givenCode == null) {
            OAuth20Utils.revokeTokensIssuedFromCode(ticketRegistry, code);
        }
        val oauthCode = givenCode == null || givenCode.isExpired() ? null : givenCode;
        val clientId = oauthCode == null ? request.getParameter(OAuth20Constants.CLIENT_ID) : oauthCode.getClientId();
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId, OidcRegisteredService.class);
        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
        return accessResult.isExecutionFailure() ? null : registeredService;
    }

    protected @Nullable OAuth20UserProfile determineUserProfile(final String assertion, final JwtConsumer consumer) {
        return FunctionUtils.doAndHandle(() -> {
            val jwt = consumer.processToClaims(assertion);
            val userProfile = new DefaultOAuth20UserProfile(jwt.getSubject(), OAuth20UserProfile.COMMON_PROFILE_HANDLER_NAME);
            userProfile.addAttributes(jwt.getClaimsMap());
            return userProfile;
        }, _ -> null).get();
    }

    protected boolean registerJwtIdentifier(final JWT jwt, final String clientId) throws Exception {
        val claims = jwt.getJWTClaimsSet();
        val jwtId = Objects.requireNonNull(claims.getJWTID());
        val hashedJwtId = DigestUtils.sha256(jwtId);
        val key = OAuth20Constants.CLIENT_ASSERTION + ':' + clientId + ':' + hashedJwtId;
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(key);
        try {
            val ticket = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            if (ticket != null && !ticket.isExpired()) {
                return false;
            }
        } catch (final InvalidTicketException e) {
            LOGGER.trace("Client assertion JWT identifier [{}] has not been used", jwtId);
        }
        val expirationTime = Objects.requireNonNull(claims.getExpirationTime()).toInstant();
        val timeToLive = Math.max(1, Duration.between(Instant.now(Clock.systemUTC()), expirationTime).toSeconds());
        val properties = new HashMap<String, Serializable>();
        properties.put(OAuth20Constants.CLIENT_ID, clientId);
        properties.put("jti", hashedJwtId);
        properties.put(ExpirationPolicy.class.getName(), new HardTimeoutExpirationPolicy(timeToLive));
        val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(ticketId, properties);
        FunctionUtils.doUnchecked(_ -> ticketRegistry.addTicket(ticket));
        return true;
    }

    /**
     * Determine whether a key can verify the client assertion.
     * An HMAC-signed assertion needs a shared key at least as long as the hash output
     * of its algorithm, i.e. 32 bytes for {@code HS256}, 48 for {@code HS384} and 64 for {@code HS512}
     * (RFC 7518, section 3.2). The verifier rejects a shorter key, so it is skipped with a warning
     * that names the application instead of failing deep inside the verification.
     * Keys of any other type, and keys for assertions signed with any other algorithm, are left to the verifier.
     *
     * @param jwt               the client assertion
     * @param jsonWebKey        the candidate verification key
     * @param registeredService the registered service
     * @return true if the key may be used to verify the assertion
     */
    protected boolean isVerificationKeyUsable(final JWT jwt, final JsonWebKey jsonWebKey,
                                              final OidcRegisteredService registeredService) {
        val algorithm = JWSAlgorithm.parse(jwt.getHeader().getAlgorithm().getName());
        if (!JWSAlgorithm.Family.HMAC_SHA.contains(algorithm) || !(jsonWebKey instanceof final OctetSequenceJsonWebKey sharedKey)) {
            return true;
        }
        val requiredLength = Integer.parseInt(algorithm.getName().substring(2)) / Byte.SIZE;
        val keyLength = sharedKey.getOctetSequence().length;
        if (keyLength < requiredLength) {
            LOGGER.warn("Skipping a shared key of [{}] bytes for [{}]: [{}] requires a client secret of at least [{}] bytes",
                keyLength, registeredService.getName(), algorithm.getName(), requiredLength);
            return false;
        }
        return true;
    }

    protected boolean validateJwtAlgorithm(final Algorithm alg) {
        val jwsAlgorithm = JWSAlgorithm.parse(alg.getName());
        return JWSAlgorithm.Family.HMAC_SHA.contains(jwsAlgorithm)
            || JWSAlgorithm.Family.RSA.contains(jwsAlgorithm)
            || JWSAlgorithm.Family.EC.contains(jwsAlgorithm)
            || JWSAlgorithm.Family.ED.contains(jwsAlgorithm);
    }
}
