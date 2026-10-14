package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20DPoPNonceService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.crypto.CertUtils;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPIssuer;
import com.nimbusds.oauth2.sdk.dpop.verifiers.DPoPTokenRequestVerifier;
import com.nimbusds.oauth2.sdk.id.ClientID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.http.BadRequestAction;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.security.cert.X509Certificate;

/**
 * Authenticates clients by a client attestation and its proof of possession, per OAuth 2.0 Attestation-Based Client
 * Authentication ({@code attest_jwt_client_auth}), which wallets use with a wallet attestation (OpenID4VCI 1.0
 * Appendix E). The client attestation must be signed by the key of the first certificate in its {@code x5c} header,
 * with a chain that leads to a configured trust anchor without including it and a signer that is not self-signed
 * (HAIP 1.0 section 4.4.1). Its {@code sub} is the client identifier, and its {@code cnf} key must verify the proof of
 * possession, whose {@code aud} is the issuer and whose {@code jti} may be used once while its {@code iat} is recent.
 * Once challenges are turned on, the proof must also carry a challenge CAS handed out; otherwise the request is answered
 * with {@code use_attestation_challenge} and a fresh challenge. In the DPoP combined mode ({@code attest_jwt_client_auth_dpop})
 * the request carries no proof of possession header, and a DPoP proof made with the attested key takes its place; once DPoP
 * nonces are turned on, it must carry one, or the request is answered with {@code use_dpop_nonce} and a fresh nonce.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcClientAttestationAuthenticator implements Authenticator {
    /**
     * Header that carries the client attestation.
     */
    public static final String HEADER_CLIENT_ATTESTATION = "OAuth-Client-Attestation";

    /**
     * Header that carries the proof of possession of the client attestation key.
     */
    public static final String HEADER_CLIENT_ATTESTATION_POP = "OAuth-Client-Attestation-PoP";

    private static final JOSEObjectType CLIENT_ATTESTATION_TYPE = new JOSEObjectType("oauth-client-attestation+jwt");

    private static final JOSEObjectType CLIENT_ATTESTATION_POP_TYPE = new JOSEObjectType("oauth-client-attestation-pop+jwt");

    private static final int CLOCK_SKEW_SECONDS = 30;

    private static final int PROOF_OF_POSSESSION_LIFETIME_SECONDS = 300;

    private static final String PROOF_OF_POSSESSION_PREFIX = "client_attestation_pop:";

    private static final String DPOP_PROOF_PREFIX = "client_attestation_dpop:";

    protected final ServicesManager servicesManager;

    protected final AuditableExecution registeredServiceAccessStrategyEnforcer;

    protected final TicketRegistry ticketRegistry;

    protected final TicketFactory ticketFactory;

    protected final CasConfigurationProperties casProperties;

    protected final OidcServerDiscoverySettings oidcServerDiscoverySettings;

    protected final OidcClientAttestationChallengeService challengeService;

    protected final OAuth20DPoPNonceService dpopNonceService;

    private final Set<TrustAnchor> trustAnchors;

    public OidcClientAttestationAuthenticator(final ServicesManager servicesManager,
                                              final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                              final TicketRegistry ticketRegistry,
                                              final TicketFactory ticketFactory,
                                              final CasConfigurationProperties casProperties,
                                              final OidcServerDiscoverySettings oidcServerDiscoverySettings,
                                              final OidcClientAttestationChallengeService challengeService,
                                              final OAuth20DPoPNonceService dpopNonceService) {
        this.servicesManager = servicesManager;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.ticketRegistry = ticketRegistry;
        this.ticketFactory = ticketFactory;
        this.casProperties = casProperties;
        this.oidcServerDiscoverySettings = oidcServerDiscoverySettings;
        this.challengeService = challengeService;
        this.dpopNonceService = dpopNonceService;
        this.trustAnchors = CertUtils.readTrustAnchors(casProperties.getAuthn().getOidc().getClientAttestation().getTrustAnchors());
    }

    /**
     * Authenticate the client named by the client attestation. The {@code client_id} request parameter, when present,
     * must match the attestation's {@code sub}, and the registered service must allow access and, at the token
     * endpoint, accept {@code attest_jwt_client_auth}, or {@code attest_jwt_client_auth_dpop} in the DPoP combined mode, when
     * it names a token endpoint authentication method. A failed check leaves the request unauthenticated by this client,
     * except a missing or invalid challenge, which is answered with {@code use_attestation_challenge}, and a DPoP proof without
     * a valid nonce in the DPoP combined mode, which is answered with {@code use_dpop_nonce}.
     *
     * @param callContext the call context
     * @param credentials the credentials
     * @return the credentials with the client profile, or empty
     */
    @Override
    public Optional<Credentials> validate(final CallContext callContext, final Credentials credentials) {
        if (!(credentials instanceof final ClientAttestationCredentials attestationCredentials) || trustAnchors.isEmpty()) {
            return Optional.empty();
        }
        try {
            val clientAttestation = verifyClientAttestation(attestationCredentials.getClientAttestation());
            val clientId = clientAttestation.clientId();
            val requestedClientId = callContext.webContext().getRequestParameter(OAuth20Constants.CLIENT_ID);
            if (requestedClientId.isPresent() && !clientId.equals(requestedClientId.get())) {
                throw new CredentialsException("Client attestation sub does not match the client_id of the request");
            }
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId, OidcRegisteredService.class);
            val accessResult = registeredServiceAccessStrategyEnforcer.execute(AuditableContext.builder().registeredService(registeredService).build());
            if (registeredService == null || accessResult.isExecutionFailure()) {
                throw new CredentialsException("No registered service allows access to client " + clientId);
            }
            val method = attestationCredentials.isDpopCombinedMode()
                ? OAuth20ClientAuthenticationMethods.ATTEST_JWT_CLIENT_AUTH_DPOP
                : OAuth20ClientAuthenticationMethods.ATTEST_JWT_CLIENT_AUTH;
            if (!OAuth20Utils.isTokenAuthenticationMethodSupportedFor(callContext, registeredService, method)) {
                throw new CredentialsException("Client authentication method " + method.getType() + " is not supported for service " + registeredService.getName());
            }
            if (attestationCredentials.isDpopCombinedMode()) {
                verifyDPoPProof(callContext.webContext(), Objects.requireNonNull(attestationCredentials.getDpopProof()), clientAttestation.key(), clientId);
            } else {
                verifyProofOfPossession(callContext.webContext(), Objects.requireNonNull(attestationCredentials.getProofOfPossession()),
                    clientAttestation.key(), clientId);
            }

            val profile = new CommonProfile();
            profile.setId(clientId);
            profile.addAttribute(OAuth20Constants.CLIENT_ID, clientId);
            credentials.setUserProfile(profile);
            return Optional.of(credentials);
        } catch (final HttpAction e) {
            throw e;
        } catch (final Throwable e) {
            LOGGER.warn("Unable to authenticate client by its client attestation: [{}]", e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Verify the client attestation: its type, an accepted signing algorithm, its {@code x5c} chain to a trust anchor
     * and its signature, a {@code sub}, an {@code exp} that has not passed, an {@code iat}, if any, not in the future,
     * and a public {@code cnf} key. A {@code status} claim is accepted with a warning, since the status is not checked.
     *
     * @param clientAttestation the client attestation JWT
     * @return the client identifier and the key of the client instance
     * @throws Exception the exception
     */
    protected ClientAttestation verifyClientAttestation(final String clientAttestation) throws Exception {
        val signedJwt = SignedJWT.parse(clientAttestation);
        verifyHeader(signedJwt, CLIENT_ATTESTATION_TYPE);
        val encodedChain = signedJwt.getHeader().getX509CertChain();
        if (encodedChain == null || encodedChain.isEmpty()) {
            throw new CredentialsException("Client attestation carries no x5c certificate chain");
        }
        val chain = new ArrayList<X509Certificate>();
        for (val encoded : encodedChain) {
            chain.add(Objects.requireNonNull(X509CertUtils.parse(encoded.decode()), "Client attestation x5c holds an invalid certificate"));
        }
        val signer = CertUtils.validateCertificateChain(chain, trustAnchors);
        verifySignature(signedJwt, JWK.parse(signer));

        val claims = signedJwt.getJWTClaimsSet();
        val now = Instant.now(Clock.systemUTC());
        if (StringUtils.isBlank(claims.getSubject())) {
            throw new CredentialsException("Client attestation carries no sub");
        }
        if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().plusSeconds(CLOCK_SKEW_SECONDS).isBefore(now)) {
            throw new CredentialsException("Client attestation exp is missing or has passed");
        }
        if (claims.getIssueTime() != null && claims.getIssueTime().toInstant().isAfter(now.plusSeconds(CLOCK_SKEW_SECONDS))) {
            throw new CredentialsException("Client attestation iat is in the future");
        }
        val key = readConfirmationKey(claims);
        if (claims.getClaim("status") != null) {
            LOGGER.warn("Client attestation for [{}] from [{}] carries a status claim; its status is not checked",
                claims.getSubject(), signer.getSubjectX500Principal().getName());
        }
        return new ClientAttestation(claims.getSubject(), key);
    }

    /**
     * Verify the proof of possession: its type, an accepted signing algorithm, a signature by the attested key, an
     * {@code aud} naming the issuer, an {@code iat} within the last few minutes, a {@code challenge} handed out by CAS when
     * challenges are turned on, and a {@code jti} not used before.
     *
     * @param webContext        the web context
     * @param proofOfPossession the proof of possession JWT
     * @param key               the attested key
     * @param clientId          the client identifier
     * @throws Throwable the throwable
     */
    protected void verifyProofOfPossession(final WebContext webContext, final String proofOfPossession,
                                           final JWK key, final String clientId)
            throws Throwable {
        val signedJwt = SignedJWT.parse(proofOfPossession);
        verifyHeader(signedJwt, CLIENT_ATTESTATION_POP_TYPE);
        verifySignature(signedJwt, key);
        val claims = signedJwt.getJWTClaimsSet();
        if (claims.getAudience() == null || !claims.getAudience().contains(oidcServerDiscoverySettings.getIssuer())) {
            throw new CredentialsException("Client attestation proof of possession aud does not name the issuer");
        }
        val now = Instant.now(Clock.systemUTC());
        val issuedAt = claims.getIssueTime();
        if (issuedAt == null || issuedAt.toInstant().isAfter(now.plusSeconds(CLOCK_SKEW_SECONDS))
            || issuedAt.toInstant().isBefore(now.minusSeconds(PROOF_OF_POSSESSION_LIFETIME_SECONDS))) {
            throw new CredentialsException("Client attestation proof of possession iat is missing or out of range");
        }
        if (challengeService.isEnabled() && !challengeService.isValid(claims.getStringClaim("challenge"))) {
            throw useAttestationChallenge(webContext);
        }
        if (StringUtils.isBlank(claims.getJWTID()) || !registerProofOfPossessionIdentifier(PROOF_OF_POSSESSION_PREFIX, clientId, claims.getJWTID())) {
            throw new CredentialsException("Client attestation proof of possession jti is missing or has been used before");
        }
    }

    /**
     * Verify the DPoP proof that stands for the proof of possession in the DPoP combined mode: valid per RFC 9449 for this
     * request ({@code typ}, an accepted algorithm, its signature by its own {@code jwk}, {@code htm}, {@code htu} and a recent
     * {@code iat}), made with the attested key, and with a {@code jti} not used before for client authentication. The proof
     * is checked here because endpoints such as the pushed authorization request endpoint do not validate DPoP proofs
     * themselves; the token endpoint still does, to bind the access token. Server-provided challenges do not apply; once DPoP
     * nonces are turned on, the proof must carry a valid one instead (RFC 9449, section 8).
     *
     * @param webContext the web context
     * @param dpopProof  the DPoP proof
     * @param key        the attested key
     * @param clientId   the client identifier
     * @throws Throwable the throwable
     */
    protected void verifyDPoPProof(final WebContext webContext, final String dpopProof, final JWK key, final String clientId) throws Throwable {
        val signedProof = SignedJWT.parse(dpopProof);
        val oidc = casProperties.getAuthn().getOidc();
        val algorithms = oidc.getDiscovery().getDpopSigningAlgValuesSupported().stream()
            .map(JWSAlgorithm::parse)
            .collect(Collectors.toSet());
        val maximumAge = Beans.newDuration(oidc.getCore().getSkew()).toSeconds();
        val verifier = new DPoPTokenRequestVerifier(algorithms, new URI(webContext.getRequestURL()), maximumAge, maximumAge, null);
        val confirmation = verifier.verify(new DPoPIssuer(new ClientID(clientId)), signedProof, dpopNonceService.getAcceptedNonces(signedProof));
        if (!confirmation.getValue().equals(key.computeThumbprint())) {
            throw new CredentialsException("DPoP proof key does not match the client attestation key");
        }
        if (!dpopNonceService.isAccepted(signedProof)) {
            dpopNonceService.provide(webContext);
            throw badRequest(webContext, OAuth20Constants.USE_DPOP_NONCE, "A server-provided nonce is required in the DPoP proof");
        }
        val jwtId = signedProof.getJWTClaimsSet().getJWTID();
        if (StringUtils.isBlank(jwtId) || !registerProofOfPossessionIdentifier(DPOP_PROOF_PREFIX, clientId, jwtId)) {
            throw new CredentialsException("DPoP proof jti is missing or has been used before");
        }
    }

    /**
     * The {@code use_attestation_challenge} error, with a fresh challenge in the {@code OAuth-Client-Attestation-Challenge}
     * header, which the client is to put in a new proof of possession.
     *
     * @param webContext the web context
     * @return the error, to be thrown
     * @throws Throwable the throwable
     */
    protected HttpAction useAttestationChallenge(final WebContext webContext) throws Throwable {
        LOGGER.info("Client attestation proof of possession carries no valid challenge; a fresh challenge is provided");
        webContext.setResponseHeader(OidcConstants.HEADER_CLIENT_ATTESTATION_CHALLENGE, challengeService.create());
        return badRequest(webContext, OidcConstants.USE_ATTESTATION_CHALLENGE,
            "A fresh challenge is required in the client attestation proof of possession");
    }

    /**
     * A {@code 400} error that is not to be cached, kept as the response by pac4j when thrown from the authenticator.
     *
     * @param webContext  the web context
     * @param error       the error code
     * @param description the error description
     * @return the error, to be thrown
     */
    protected static HttpAction badRequest(final WebContext webContext, final String error, final String description) {
        webContext.setResponseHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        webContext.setResponseContentType(MediaType.APPLICATION_JSON_VALUE);
        val action = new BadRequestAction();
        action.setContent(JSONObjectUtils.toJSONString(new LinkedHashMap<>(OAuth20Utils.getErrorResponseBody(error, description))));
        return action;
    }

    protected void verifyHeader(final SignedJWT signedJwt, final JOSEObjectType type) {
        if (!type.equals(signedJwt.getHeader().getType())) {
            throw new CredentialsException("JWT type must be " + type);
        }
        val algorithm = signedJwt.getHeader().getAlgorithm();
        if (!casProperties.getAuthn().getOidc().getClientAttestation().getSigningAlgValuesSupported().contains(algorithm.getName())) {
            throw new CredentialsException("JWT signing algorithm " + algorithm + " is not accepted");
        }
    }

    protected static void verifySignature(final SignedJWT signedJwt, final JWK key) throws Exception {
        val algorithm = signedJwt.getHeader().getAlgorithm();
        val verified = switch (key) {
            case final ECKey ecKey when JWSAlgorithm.Family.EC.contains(algorithm) -> signedJwt.verify(new ECDSAVerifier(ecKey));
            case final RSAKey rsaKey when JWSAlgorithm.Family.RSA.contains(algorithm) -> signedJwt.verify(new RSASSAVerifier(rsaKey));
            default -> false;
        };
        if (!verified) {
            throw new CredentialsException("JWT signature validation failed");
        }
    }

    protected static JWK readConfirmationKey(final JWTClaimsSet claims) throws Exception {
        val confirmation = claims.getJSONObjectClaim("cnf");
        if (confirmation == null || !(confirmation.get("jwk") instanceof final Map<?, ?> members)) {
            throw new CredentialsException("Client attestation carries no cnf jwk");
        }
        val key = JWK.parse((Map<String, Object>) members);
        if (key.isPrivate()) {
            throw new CredentialsException("Client attestation cnf jwk must not contain private key material");
        }
        return key;
    }

    protected boolean registerProofOfPossessionIdentifier(final String prefix, final String clientId, final String jwtId) throws Exception {
        val hashedJwtId = DigestUtils.sha256(jwtId);
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(prefix + clientId + ':' + hashedJwtId);
        val ticket = ticketRegistry.getTicket(ticketId);
        if (ticket != null && !ticket.isExpired()) {
            return false;
        }
        val properties = new HashMap<String, Serializable>();
        properties.put(OAuth20Constants.CLIENT_ID, clientId);
        properties.put(ExpirationPolicy.class.getName(),
            new HardTimeoutExpirationPolicy(PROOF_OF_POSSESSION_LIFETIME_SECONDS + CLOCK_SKEW_SECONDS));
        val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        ticketRegistry.addTicket(factory.create(ticketId, properties));
        return true;
    }

    /**
     * A verified client attestation.
     *
     * @param clientId the client identifier, from {@code sub}
     * @param key      the key of the client instance, from {@code cnf}
     */
    protected record ClientAttestation(String clientId, JWK key) {
    }

    /**
     * The client attestation and its proof of possession, as presented in their headers: a client attestation proof of
     * possession, or, in the DPoP combined mode, a DPoP proof.
     */
    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ClientAttestationCredentials extends Credentials {
        @Serial
        private static final long serialVersionUID = -2271463547431093327L;

        private final String clientAttestation;

        private final @Nullable String proofOfPossession;

        private final @Nullable String dpopProof;

        /**
         * Whether a DPoP proof stands for the proof of possession.
         *
         * @return true in the DPoP combined mode
         */
        public boolean isDpopCombinedMode() {
            return proofOfPossession == null;
        }
    }

    /**
     * Extracts the client attestation and its proof of possession, each of which must arrive in exactly one header. A
     * request with a client attestation and a single DPoP proof but no proof of possession header uses the DPoP combined
     * mode; with a proof of possession header, a DPoP proof is left to the DPoP validation of the endpoint.
     */
    public static class ClientAttestationCredentialsExtractor implements CredentialsExtractor {
        @Override
        public Optional<Credentials> extract(final CallContext callContext) {
            if (callContext.webContext() instanceof final JEEContext context) {
                val attestations = readHeaders(context, HEADER_CLIENT_ATTESTATION);
                val proofs = readHeaders(context, HEADER_CLIENT_ATTESTATION_POP);
                val dpopProofs = readHeaders(context, OAuth20Constants.DPOP);
                if (attestations.size() == 1 && StringUtils.isNotBlank(attestations.getFirst())) {
                    if (proofs.size() == 1 && StringUtils.isNotBlank(proofs.getFirst())) {
                        return Optional.of(new ClientAttestationCredentials(attestations.getFirst().trim(), proofs.getFirst().trim(), null));
                    }
                    if (proofs.isEmpty() && dpopProofs.size() == 1 && StringUtils.isNotBlank(dpopProofs.getFirst())) {
                        return Optional.of(new ClientAttestationCredentials(attestations.getFirst().trim(), null, dpopProofs.getFirst().trim()));
                    }
                }
            }
            return Optional.empty();
        }

        private static List<String> readHeaders(final JEEContext context, final String headerName) {
            val headers = context.getNativeRequest().getHeaders(headerName);
            return headers == null ? List.of() : Collections.list(headers);
        }
    }
}
