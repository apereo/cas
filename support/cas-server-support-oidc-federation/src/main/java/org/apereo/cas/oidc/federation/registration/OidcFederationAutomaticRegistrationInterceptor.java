package org.apereo.cas.oidc.federation.registration;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.chain.OidcFederationResolutionUnavailableException;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.oidc.web.OidcRequestObjectDecrypter;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DigestUtils;
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
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Runs ahead of the OAuth/OIDC interceptors on the authorization and pushed authorization endpoints, so that a relying
 * party of the federation is registered automatically (OpenID Federation 1.0, section 12.1) before client
 * authentication and the authorization request validators look the client up.
 * <p>
 * The client is the {@code client_id} parameter, or, for an authorization request without one, the {@code client_id}
 * of its request object, which is where the authorization request validators take it from; a request object whose
 * {@code client_id} differs from the parameter is refused by the request parameter resolver. The interceptor runs on
 * the OpenID Connect endpoints and on the OAuth authorization endpoint.
 * <p>
 * An authorization request of an automatically registered client must prove that the client controls its keys
 * (section 12.1.1): it carries a request object signed with a key of the relying party's resolved metadata, whose
 * {@code iss} and {@code client_id} are the Entity Identifier, whose {@code aud} is only the OP's Entity Identifier,
 * with {@code jti} and {@code exp} and without {@code sub}, valid for 10 minutes at most. A request object is used
 * once: its {@code jti} is kept in the ticket registry for as long as the request object could be accepted, and at least
 * for the lifetime of transient session tickets, and only the very same request object may come back in that time, which
 * is what the authorization endpoint receives again after the login, even once the request object has expired. A pushed authorization
 * request proves the keys through client authentication instead, which the pushed authorization endpoint requires of an
 * automatically registered client since its registration only allows asymmetric methods, so the authorization request
 * that refers to it by {@code request_uri} carries no request object; a client assertion sent there must have the OP
 * Entity Identifier as its only {@code aud} (section 12.1.1.2), or the request is refused with {@code invalid_client}. A request object encrypted to CAS is decrypted
 * first and its nested JWT checked as above, while replay is tracked on the request object as sent. Requests that fail
 * are refused with {@code invalid_request} and are never redirected to the client, since its trust could not be
 * established. A relying party that is not registered yet and cannot be registered now, because registrations are busy
 * or what its registration depends on cannot be reached, is answered with {@code 503} and
 * {@code temporarily_unavailable} rather than as an unknown client.
 * <p>
 * A relying party that rolls its keys over signs with keys its registration does not hold until the registration is
 * resolved again. A request object of an authorization request, or a {@code client_assertion} sent to any of the
 * OpenID Connect endpoints, that verifies with none of the signing keys of an automatically registered client has the
 * registration reevaluated first (section 12.3), before the request object is checked and before client
 * authentication; the client is the {@code client_id}, or the subject of the client assertion when there is none.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcFederationAutomaticRegistrationInterceptor implements HandlerInterceptor {
    private static final String REQUEST_OBJECT_TICKET_PREFIX = "OidcFederationRequestObject:";

    private static final String REQUEST_OBJECT_DIGEST = "requestObjectDigest";

    private static final int ALLOWED_CLOCK_SKEW_SECONDS = 30;

    /**
     * The longest a request object may be valid for, from now or from its {@code iat}: it must be kept as used for as
     * long as it could be accepted, so its lifetime bounds what is remembered.
     */
    private static final long MAX_REQUEST_OBJECT_LIFETIME_SECONDS = Duration.ofMinutes(10).toSeconds();

    private static final String TRUST_CHAIN = "trust_chain";

    private static final String INVALID_CLIENT = "invalid_client";

    private static final String TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";

    private final OidcFederationAutomaticClientRegistration clientRegistration;

    private final OidcIssuerService issuerService;

    private final TicketRegistry ticketRegistry;

    private final TicketFactory ticketFactory;

    private final ApplicationContext applicationContext;

    private final ObjectProvider<OidcRequestObjectDecrypter> requestObjectDecrypter;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler)
            throws Exception {
        val clientAssertion = resolveClientAssertion(request);
        val authorizationRequest = isAuthorizationRequest(request);
        val clientId = StringUtils.firstNonBlank(request.getParameter(OAuth20Constants.CLIENT_ID),
            authorizationRequest ? resolveRequestObjectClientId(request) : null, resolveSubject(clientAssertion));
        if ((!authorizationRequest && clientAssertion == null) || !OidcFederationAutomaticClientRegistration.isFederatedEntityId(clientId)) {
            return true;
        }
        val signedObjects = Stream.of(authorizationRequest ? resolveRequestObject(request) : null, clientAssertion)
            .filter(Objects::nonNull)
            .toList();
        val trustChain = resolveTrustChain(signedObjects);
        var registeredService = Optional.<OAuthRegisteredService>empty();
        if (authorizationRequest) {
            try {
                registeredService = Optional.ofNullable(clientRegistration.register(clientId, trustChain,
                    service -> !signedObjects.isEmpty() && isSignedWithKeysOf(service, signedObjects)));
            } catch (final OidcFederationResolutionUnavailableException e) {
                LOGGER.warn("Relying party [{}] cannot be registered now: [{}]", clientId, e.getMessage());
                return refuse(response, HttpStatus.SERVICE_UNAVAILABLE, TEMPORARILY_UNAVAILABLE, "Relying party cannot be registered now; try again later");
            }
        }
        if (!signedObjects.isEmpty()) {
            registeredService = Optional.ofNullable(clientRegistration.reevaluate(clientId, service -> !isSignedWithKeysOf(service, signedObjects), trustChain));
        }
        if (!authorizationRequest || !(registeredService.orElse(null) instanceof final OidcRegisteredService oidcService)
            || !OidcFederationAutomaticClientRegistration.isAutomaticallyRegistered(oidcService)) {
            return true;
        }
        if (isPushedAuthorizationRequest(request)) {
            if (clientAssertion != null && !isAddressedOnlyTo(clientAssertion, issuerService.determineIssuer(Optional.of(oidcService)))) {
                LOGGER.warn("Refusing the pushed authorization request of [{}]: its client assertion is not addressed only to CAS", clientId);
                return refuse(response, HttpStatus.UNAUTHORIZED, INVALID_CLIENT, "Client assertion aud must only be the OP Entity Identifier");
            }
            return true;
        }
        if (StringUtils.isBlank(request.getParameter(OidcConstants.REQUEST_URI))) {
            val error = verifyRequestObject(request, oidcService);
            if (error.isPresent()) {
                LOGGER.warn("Refusing the authorization request of [{}]: [{}]", clientId, error.get());
                return refuse(response, HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, error.get());
            }
        }
        return true;
    }

    private static boolean refuse(final HttpServletResponse response, final HttpStatus status, final String error,
                                  final String description)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(OAuth20Utils.toJson(OAuth20Utils.getErrorResponseBody(error, description)));
        return false;
    }

    /**
     * Whether the client assertion is addressed only to the given entity: at the pushed authorization endpoint, the
     * client assertion of an automatically registered relying party must have the OP Entity Identifier as its only
     * audience (OpenID Federation 1.0, section 12.1.1.2), not its token endpoint nor any other value besides.
     *
     * @param clientAssertion the client assertion
     * @param entityId        the entity identifier of CAS
     * @return true or false
     */
    private static boolean isAddressedOnlyTo(final String clientAssertion, final String entityId) {
        try {
            return List.of(entityId).equals(JWTParser.parse(clientAssertion).getJWTClaimsSet().getAudience());
        } catch (final ParseException e) {
            LOGGER.trace("Client assertion cannot be parsed: [{}]", e.getMessage());
            return false;
        }
    }

    /**
     * Verify the request object of an authorization request sent by an automatically registered client.
     *
     * @param request           the request
     * @param registeredService the automatically registered service
     * @return the reason the request is refused, or empty when the request object proves the client's keys
     */
    protected Optional<String> verifyRequestObject(final HttpServletRequest request, final OidcRegisteredService registeredService) {
        val requestObject = request.getParameter(OAuth20Constants.REQUEST);
        if (StringUtils.isBlank(requestObject)) {
            return Optional.of("Automatic registration requires a signed request object or a pushed authorization request");
        }
        try {
            val nestedRequestObject = decryptRequestObject(requestObject);
            val jwtId = JWTParser.parse(nestedRequestObject).getJWTClaimsSet().getJWTID();
            if (StringUtils.isBlank(jwtId)) {
                return Optional.of("Request object has no jti");
            }
            val clientId = registeredService.getClientId();
            val ticketId = TransientSessionTicketFactory.normalizeTicketId(
                REQUEST_OBJECT_TICKET_PREFIX + clientId + ':' + DigestUtils.sha256(jwtId));
            val digest = DigestUtils.sha256(requestObject);
            val usedTicket = findUsedRequestObject(ticketId);
            if (usedTicket != null) {
                return Strings.CS.equals(digest, usedTicket.getPropertyAsString(REQUEST_OBJECT_DIGEST))
                    ? Optional.empty()
                    : Optional.of("Request object jti has already been used");
            }
            val error = verifyRequestObjectClaims(nestedRequestObject, registeredService);
            if (error.isEmpty()) {
                val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
                val properties = new HashMap<String, Serializable>();
                properties.put(OAuth20Constants.CLIENT_ID, clientId);
                properties.put(REQUEST_OBJECT_DIGEST, digest);
                properties.put(ExpirationPolicy.class.getName(), new HardTimeoutExpirationPolicy(
                    usedRequestObjectLifetime(factory, JWTParser.parse(nestedRequestObject).getJWTClaimsSet().getExpirationTime())));
                ticketRegistry.addTicket(factory.create(ticketId, properties));
            }
            return error;
        } catch (final Throwable e) {
            LOGGER.debug("Unable to verify the request object of [{}]: [{}]", registeredService.getClientId(), e.getMessage());
            return Optional.of("Request object is invalid");
        }
    }

    /**
     * Whether every signed object verifies with one of the signing keys of the registration, by signature alone.
     *
     * @param registeredService the registered service
     * @param signedObjects     the signed JWTs the client sent
     * @return true or false
     */
    protected boolean isSignedWithKeysOf(final OAuthRegisteredService registeredService, final List<String> signedObjects) {
        if (!(registeredService instanceof final OidcRegisteredService oidcService)) {
            return true;
        }
        val keys = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(oidcService, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING))
            .map(JsonWebKeySet::getJsonWebKeys)
            .orElseGet(List::of);
        return signedObjects.stream().allMatch(signedObject -> keys.stream().anyMatch(key -> isSignedWith(signedObject, key)));
    }

    private static boolean isSignedWith(final String signedObject, final JsonWebKey key) {
        try {
            val signature = new JsonWebSignature();
            signature.setCompactSerialization(signedObject);
            signature.setKey(key.getKey());
            return signature.verifySignature();
        } catch (final JoseException e) {
            LOGGER.trace("Signed object does not verify with key [{}]: [{}]", key.getKeyId(), e.getMessage());
            return false;
        }
    }

    /**
     * The trust chain the relying party sent in the {@code trust_chain} header (OpenID Federation 1.0, section 4.3) of
     * its request object, or else of its client assertion. It is only a candidate, read before the signature of the
     * object is checked: the chain is verified, and used only when the objects the client sent are signed with the
     * keys it resolves to.
     *
     * @param signedObjects the signed JWTs the client sent, the request object first
     * @return the serialized statements, or an empty list
     */
    private static List<String> resolveTrustChain(final List<String> signedObjects) {
        return signedObjects
            .stream()
            .map(Unchecked.function(signedObject -> SignedJWT.parse(signedObject).getHeader().getCustomParam(TRUST_CHAIN)))
            .filter(value -> value instanceof final List<?> statements && !statements.isEmpty()
                && statements.stream().allMatch(String.class::isInstance))
            .findFirst()
            .map(value -> ((List<?>) value).stream().map(String.class::cast).toList())
            .orElseGet(List::of);
    }

    private static @Nullable String resolveClientAssertion(final HttpServletRequest request) {
        val assertion = request.getParameter(OAuth20Constants.CLIENT_ASSERTION);
        return Strings.CI.equals(OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER, request.getParameter(OAuth20Constants.CLIENT_ASSERTION_TYPE))
            && isSignedJwt(assertion) ? assertion : null;
    }

    private @Nullable String resolveRequestObject(final HttpServletRequest request) {
        val requestObject = request.getParameter(OAuth20Constants.REQUEST);
        if (StringUtils.isBlank(requestObject)) {
            return null;
        }
        try {
            val nestedRequestObject = decryptRequestObject(requestObject);
            return isSignedJwt(nestedRequestObject) ? nestedRequestObject : null;
        } catch (final Exception e) {
            LOGGER.trace("Request object cannot be decrypted: [{}]", e.getMessage());
            return null;
        }
    }

    /**
     * The {@code client_id} of the request object, signed or not, when the request carries no {@code client_id}
     * parameter: the authorization request validators then take the client from the request object, so the client
     * whose keys must be proven is that one.
     *
     * @param request the request
     * @return the client id of the request object, or null
     */
    private @Nullable String resolveRequestObjectClientId(final HttpServletRequest request) {
        val requestObject = request.getParameter(OAuth20Constants.REQUEST);
        if (StringUtils.isBlank(requestObject)) {
            return null;
        }
        try {
            return JWTParser.parse(decryptRequestObject(requestObject)).getJWTClaimsSet().getStringClaim(OAuth20Constants.CLIENT_ID);
        } catch (final Exception e) {
            LOGGER.trace("Request object has no client id: [{}]", e.getMessage());
            return null;
        }
    }

    private static @Nullable String resolveSubject(@Nullable final String clientAssertion) {
        try {
            return clientAssertion == null ? null : JWTParser.parse(clientAssertion).getJWTClaimsSet().getSubject();
        } catch (final ParseException e) {
            LOGGER.trace("Client assertion has no subject: [{}]", e.getMessage());
            return null;
        }
    }

    private static boolean isSignedJwt(@Nullable final String value) {
        try {
            return StringUtils.isNotBlank(value) && JWTParser.parse(value) instanceof SignedJWT;
        } catch (final ParseException e) {
            LOGGER.trace("Value is not a signed JWT: [{}]", e.getMessage());
            return false;
        }
    }

    private String decryptRequestObject(final String requestObject) {
        if (!OidcRequestObjectDecrypter.isEncrypted(requestObject)) {
            return requestObject;
        }
        val decrypter = requestObjectDecrypter.getIfAvailable();
        if (decrypter == null) {
            throw new IllegalStateException("Encrypted request objects cannot be decrypted without OpenID Connect");
        }
        return decrypter.decrypt(requestObject);
    }

    /**
     * How long a used request object is remembered: until it can no longer be accepted, its expiration plus the allowed
     * clock skew, and at least for the lifetime of transient session tickets, during which the very same request object
     * comes back after the login.
     */
    private static long usedRequestObjectLifetime(final TransientSessionTicketFactory factory, final Date expiration) {
        val transientLifetime = Objects.requireNonNullElse(factory.getExpirationPolicyBuilder().buildTicketExpirationPolicy().getTimeToLive(), 0L);
        val acceptedFor = Duration.between(Instant.now(Clock.systemUTC()), expiration.toInstant()).toSeconds() + ALLOWED_CLOCK_SKEW_SECONDS;
        return Math.max(1, Math.max(transientLifetime, acceptedFor));
    }

    private Optional<String> verifyRequestObjectClaims(final String requestObject, final OidcRegisteredService registeredService) {
        val keys = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(registeredService, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING))
            .map(JsonWebKeySet::getJsonWebKeys)
            .orElseGet(List::of);
        if (keys.isEmpty()) {
            return Optional.of("Relying party metadata has no keys to verify the request object");
        }
        val clientId = registeredService.getClientId();
        val entityId = issuerService.determineIssuer(Optional.of(registeredService));
        val consumer = new JwtConsumerBuilder()
            .setVerificationKeyResolver(new JwksVerificationKeyResolver(keys))
            .setExpectedIssuer(true, clientId)
            .setExpectedAudience(true, entityId)
            .setRequireJwtId()
            .setRequireExpirationTime()
            .setAllowedClockSkewInSeconds(ALLOWED_CLOCK_SKEW_SECONDS)
            .build();
        try {
            val claims = consumer.processToClaims(requestObject);
            if (claims.getAudience().size() != 1) {
                return Optional.of("Request object aud must only be the OP Entity Identifier");
            }
            if (!Strings.CS.equals(clientId, claims.getClaimValueAsString(OAuth20Constants.CLIENT_ID))) {
                return Optional.of("Request object client_id must be the Entity Identifier of the client");
            }
            if (claims.hasClaim("sub")) {
                return Optional.of("Request object must not carry sub");
            }
            val expiration = claims.getExpirationTime().getValue();
            val now = Instant.now(Clock.systemUTC()).getEpochSecond();
            if (expiration - now > MAX_REQUEST_OBJECT_LIFETIME_SECONDS + ALLOWED_CLOCK_SKEW_SECONDS
                || (claims.getIssuedAt() != null && expiration - claims.getIssuedAt().getValue() > MAX_REQUEST_OBJECT_LIFETIME_SECONDS)) {
                return Optional.of("Request object must not be valid for more than 10 minutes");
            }
            return Optional.empty();
        } catch (final InvalidJwtException | MalformedClaimException e) {
            LOGGER.debug("Request object of [{}] failed verification: [{}]", clientId, e.getMessage());
            return Optional.of("Request object signature or claims are invalid");
        }
    }

    private @Nullable TransientSessionTicket findUsedRequestObject(final String ticketId) {
        try {
            val ticket = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            return ticket != null && !ticket.isExpired() ? ticket : null;
        } catch (final InvalidTicketException e) {
            LOGGER.trace("Request object [{}] has not been used", ticketId);
            return null;
        }
    }

    /**
     * Whether the request is an authorization or pushed authorization request, by its path as handler mappings see it:
     * a path that reaches an authorization endpoint once decoded, such as one with an encoded letter, is one.
     *
     * @param request the request
     * @return true or false
     */
    private static boolean isAuthorizationRequest(final HttpServletRequest request) {
        return isPushedAuthorizationRequest(request)
            || OidcRequestSupport.isRequestForEndpoint(request, OidcConstants.AUTHORIZE_URL)
            || OidcRequestSupport.isRequestForEndpoint(request, OAuth20Constants.AUTHORIZE_URL);
    }

    private static boolean isPushedAuthorizationRequest(final HttpServletRequest request) {
        return OidcRequestSupport.isRequestForEndpoint(request, OidcConstants.PUSHED_AUTHORIZE_URL);
    }
}
