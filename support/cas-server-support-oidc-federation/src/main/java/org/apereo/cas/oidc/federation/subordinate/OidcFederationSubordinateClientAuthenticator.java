package org.apereo.cas.oidc.federation.subordinate;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.AsymmetricJWK;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

/**
 * Client authentication of subordinates at the federation endpoints of this entity (OpenID Federation 1.0, section
 * 8.8), with {@code private_key_jwt}: the client assertion is signed with a federation key of the subordinate, as its
 * subordinate definition holds them, its {@code iss} and {@code sub} are the entity identifier of the subordinate, its
 * {@code aud} is only the entity identifier of this entity, and it carries {@code jti} and {@code exp}. A client
 * assertion is accepted once: its {@code jti} is kept in the ticket registry until the assertion can no longer be
 * accepted, that is its expiration plus the allowed clock skew. Checking and recording the {@code jti} is a single step
 * on each node; across nodes, it is as atomic as the ticket registry.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcFederationSubordinateClientAuthenticator {
    /**
     * Bean name.
     */
    public static final String BEAN_NAME = "oidcFederationSubordinateClientAuthenticator";

    private static final int RSA_KEY_SIZE = 2048;

    private static final String TICKET_PREFIX = "OidcFederationClientAssertion:";

    private static final String KEYS = "keys";

    private static final int MAX_CLOCK_SKEW_SECONDS = 60;

    private static final int LOCK_STRIPES = 64;

    private final OidcFederationSubordinateRepository subordinateRepository;

    private final TicketRegistry ticketRegistry;

    private final TicketFactory ticketFactory;

    /**
     * Locks striped by client assertion, so that checking whether a {@code jti} was used and recording it is a single step
     * on this node, and the same client assertion sent twice at once is accepted only once.
     */
    private final List<CasReentrantLock> jwtIdentifierLocks = Stream.generate(CasReentrantLock::new).limit(LOCK_STRIPES).toList();

    /**
     * Authenticate the subordinate that sent the client assertion.
     *
     * @param clientAssertionType the client assertion type
     * @param clientAssertion     the client assertion
     * @param entityId            the entity identifier of this entity, the only audience accepted
     * @return the entity identifier of the authenticated subordinate, or empty when the request does not authenticate
     */
    public Optional<String> authenticate(@Nullable final String clientAssertionType, @Nullable final String clientAssertion,
                                         final String entityId) {
        if (!Strings.CS.equals(OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER, clientAssertionType) || StringUtils.isBlank(clientAssertion)) {
            LOGGER.debug("Request carries no private_key_jwt client assertion");
            return Optional.empty();
        }
        try {
            val jwt = SignedJWT.parse(clientAssertion);
            val issuer = jwt.getJWTClaimsSet().getIssuer();
            val keys = findFederationKeys(issuer);
            if (keys.isEmpty()) {
                LOGGER.debug("Client assertion issuer [{}] is not a subordinate with federation keys", issuer);
                return Optional.empty();
            }
            val processor = new DefaultJWTProcessor<SecurityContext>();
            processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(JOSEObjectType.JWT, null,
                new JOSEObjectType("client-authentication+jwt")));
            processor.setJWSKeySelector(new JWSVerificationKeySelector<>(new HashSet<>(JWSAlgorithm.Family.SIGNATURE),
                new ImmutableJWKSet<>(keys.get())));
            val claimsVerifier = new DefaultJWTClaimsVerifier<>(entityId,
                new JWTClaimsSet.Builder().issuer(issuer).subject(issuer).build(),
                Set.of(JWTClaimNames.ISSUER, JWTClaimNames.SUBJECT, JWTClaimNames.AUDIENCE,
                    JWTClaimNames.EXPIRATION_TIME, JWTClaimNames.JWT_ID));
            claimsVerifier.setMaxClockSkew(MAX_CLOCK_SKEW_SECONDS);
            processor.setJWTClaimsSetVerifier(claimsVerifier);
            val claims = processor.process(jwt, null);
            if (claims.getAudience().size() != 1) {
                LOGGER.debug("Client assertion of [{}] is addressed to [{}], not only to [{}]", issuer, claims.getAudience(), entityId);
                return Optional.empty();
            }
            if (!registerJwtIdentifier(issuer, claims)) {
                LOGGER.warn("Client assertion of [{}] has already been used", issuer);
                return Optional.empty();
            }
            return Optional.of(issuer);
        } catch (final Exception e) {
            LOGGER.debug("Client assertion cannot be verified: [{}]", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<JWKSet> findFederationKeys(@Nullable final String entityId) throws ParseException {
        val subordinate = StringUtils.isBlank(entityId) ? null : subordinateRepository.getSubordinates().get(entityId);
        if (subordinate == null || subordinate.getFederationKeys() == null || subordinate.getFederationKeys().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(JWKSet.parse(Map.of(KEYS, JSONValue.parse(subordinate.getFederationKeys().toString()))));
    }

    private boolean registerJwtIdentifier(final String issuer, final JWTClaimsSet claims) {
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(TICKET_PREFIX + issuer + ':' + DigestUtils.sha256(claims.getJWTID()));
        return jwtIdentifierLocks.get(Math.floorMod(ticketId.hashCode(), LOCK_STRIPES))
            .execute(() -> FunctionUtils.doUnchecked(() -> registerJwtIdentifier(ticketId, issuer, claims)));
    }

    private boolean registerJwtIdentifier(final String ticketId, final String issuer, final JWTClaimsSet claims) throws Exception {
        try {
            val ticket = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            if (ticket != null && !ticket.isExpired()) {
                return false;
            }
        } catch (final InvalidTicketException e) {
            LOGGER.trace("Client assertion [{}] has not been used", ticketId);
        }
        val acceptedUntil = claims.getExpirationTime().toInstant().plusSeconds(MAX_CLOCK_SKEW_SECONDS);
        val timeToLive = Math.max(1, Duration.between(Instant.now(Clock.systemUTC()), acceptedUntil).toSeconds());
        val properties = new HashMap<String, Serializable>();
        properties.put(OAuth20Constants.CLIENT_ID, issuer);
        properties.put(ExpirationPolicy.class.getName(), new HardTimeoutExpirationPolicy(timeToLive));
        val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        ticketRegistry.addTicket(factory.create(ticketId, properties));
        return true;
    }

    /**
     * The algorithms client assertions may be signed with, published as {@code endpoint_auth_signing_alg_values_supported}:
     * the asymmetric signature algorithms whose signatures CAS can verify here, found once by signing and verifying with
     * a key generated for each. The verifier library lists algorithms that this runtime may not handle, such as
     * {@code ES256K}, whose curve the runtime may not support, or the EdDSA family, which needs a library of its own.
     *
     * @return the algorithms
     */
    public static List<String> getSigningAlgorithms() {
        return SigningAlgorithms.VERIFIABLE;
    }

    private static boolean canVerify(final JWSAlgorithm algorithm, final RSAKey rsaKey) {
        try {
            val key = JWSAlgorithm.Family.RSA.contains(algorithm) ? rsaKey : generateKey(algorithm);
            val signed = new JWSObject(new JWSHeader(algorithm), new Payload(algorithm.getName()));
            signed.sign(new DefaultJWSSignerFactory().createJWSSigner(key, algorithm));
            return signed.verify(new DefaultJWSVerifierFactory().createJWSVerifier(signed.getHeader(), ((AsymmetricJWK) key).toPublicKey()));
        } catch (final Throwable e) {
            LOGGER.debug("Signatures with [{}] cannot be verified here: [{}]", algorithm, e.getMessage());
            return false;
        }
    }

    private static JWK generateKey(final JWSAlgorithm algorithm) throws JOSEException {
        val curve = Curve.forJWSAlgorithm(algorithm).iterator().next();
        return JWSAlgorithm.Family.EC.contains(algorithm) ? new ECKeyGenerator(curve).generate() : new OctetKeyPairGenerator(curve).generate();
    }

    private static final class SigningAlgorithms {
        private static final List<String> VERIFIABLE = findVerifiableSigningAlgorithms();

        private static List<String> findVerifiableSigningAlgorithms() {
            val rsaKey = FunctionUtils.doUnchecked(() -> new RSAKeyGenerator(RSA_KEY_SIZE).generate());
            return JWSAlgorithm.Family.SIGNATURE
                .stream()
                .filter(algorithm -> canVerify(algorithm, rsaKey))
                .map(JWSAlgorithm::getName)
                .toList();
        }
    }
}
