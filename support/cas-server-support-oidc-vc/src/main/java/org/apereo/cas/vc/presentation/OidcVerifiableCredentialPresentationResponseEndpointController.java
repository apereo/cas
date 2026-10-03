package org.apereo.cas.vc.presentation;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.ClaimRequest;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.CredentialRequest;
import com.authlete.sd.Disclosure;
import com.authlete.sd.SDJWT;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jspecify.annotations.Nullable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link OidcVerifiableCredentialPresentationResponseEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialPresentationResponseEndpointController extends BaseOAuth20Controller<OidcConfigurationContext> {
    /**
     * Key binding JWT algorithms the verifier accepts and advertises as {@code kb-jwt_alg_values}. OpenID4VP 1.0
     * asks for fully specified identifiers, so Ed25519 is advertised as {@code Ed25519}; a key binding JWT that
     * still says {@code EdDSA} is accepted for an Ed25519 holder key as well.
     */
    public static final List<String> KEY_BINDING_ALGORITHMS_SUPPORTED = List.of(
        "ES256", "ES384", "ES512", "RS256", "RS384", "RS512", "PS256", "PS384", "PS512", "Ed25519");

    /**
     * Transaction property naming the client that created the presentation request.
     */
    public static final String PROPERTY_CLIENT_ID = "clientId";

    /**
     * Transaction property holding the relying party's same-device redirect URI.
     */
    public static final String PROPERTY_REDIRECT_URI = "redirectUri";

    /**
     * Result property holding the response code the relying party must present to collect the outcome.
     */
    public static final String PROPERTY_RESPONSE_CODE = "responseCode";

    /**
     * Transaction property holding the private JWK the wallet encrypts its response to, when the request asked for an
     * encrypted response.
     */
    public static final String PROPERTY_RESPONSE_ENCRYPTION_KEY = "responseEncryptionKey";

    /**
     * Content encryption algorithms accepted for encrypted responses and advertised as
     * {@code encrypted_response_enc_values_supported}, as HAIP 1.0 requires.
     */
    public static final List<String> RESPONSE_ENCRYPTION_ALGORITHMS_SUPPORTED = List.of(
        ContentEncryptionAlgorithmIdentifiers.AES_128_GCM, ContentEncryptionAlgorithmIdentifiers.AES_256_GCM);
    
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false)
        .minimal(true)
        .jsonFactory(JsonFactory.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .build())
        .build()
        .toObjectMapper();

    private static final Set<String> CREDENTIAL_JWT_TYPES = Set.of("dc+sd-jwt", "vc+sd-jwt");
    
    private static final Set<String> EDWARDS_CURVE_ALGORITHMS = Set.of("Ed25519", "EdDSA");

    private static final String STATUS_VERIFIED = "verified";

    private static final String STATUS_ERROR = "error";

    private static final Duration CLOCK_SKEW = Duration.ofSeconds(30);

    private static final int MAX_DISCLOSURE_DEPTH = 64;

    private static final int MAX_ERROR_LENGTH = 128;

    private static final int MAX_ERROR_DESCRIPTION_LENGTH = 1024;

    public OidcVerifiableCredentialPresentationResponseEndpointController(
        final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    /**
     * Handle the wallet's authorization response. It carries either a {@code vp_token} or, per OpenID4VP 1.0
     * section 8.2, an authorization error response ({@code error}, optionally {@code error_description}) when
     * the wallet declines or cannot answer. An error response is answered with {@code 200} and a JSON object as the
     * specification requires and recorded, but it does not end the request: nothing authenticates it, so anyone
     * who saw the request's {@code state} could otherwise cancel it. The request stays open until it expires, a
     * later valid presentation takes precedence, and the relying party only sees the error once the request has
     * expired unanswered.
     * <p>
     * When the request asked for an encrypted response ({@code direct_post.jwt}, OpenID4VP 1.0 section 8.3.1), the
     * parameters arrive inside the JWE posted as {@code response} and a presentation in the clear is refused; a wallet
     * that cannot encrypt may still post an error response in the clear.
     *
     * @param response         the encrypted response, for {@code direct_post.jwt}
     * @param vpToken          the vp token
     * @param error            the error code of an authorization error response
     * @param errorDescription the error description of an authorization error response
     * @param state            the state
     * @return the response entity
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL,
        "/**/" + OidcConstants.VC_PRESENTATION_RESPONSE_URL
    }, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle response for presentation request", parameters = {
        @Parameter(name = "response", description = "The encrypted response, when the request asked for direct_post.jwt"),
        @Parameter(name = "vp_token", description = "The verifiable presentation token"),
        @Parameter(name = OAuth20Constants.ERROR, description = "The error code, when the wallet answers with an error response"),
        @Parameter(name = OAuth20Constants.ERROR_DESCRIPTION, description = "The error description of an error response"),
        @Parameter(name = "state", description = "The state parameter returned from the request")
    })
    public ResponseEntity<Map<String, Object>> handleResponse(
        @RequestParam(value = "response", required = false) final @Nullable String response,
        @RequestParam(value = "vp_token", required = false) final @Nullable String vpToken,
        @RequestParam(value = OAuth20Constants.ERROR, required = false) final @Nullable String error,
        @RequestParam(value = OAuth20Constants.ERROR_DESCRIPTION, required = false) final @Nullable String errorDescription,
        @RequestParam(value = OAuth20Constants.STATE, required = false) final @Nullable String state) {

        try {
            require(StringUtils.isBlank(response) || StringUtils.isAllBlank(vpToken, error, errorDescription, state),
                "An encrypted presentation response must carry its parameters inside the response");
            val authorizationResponse = StringUtils.isNotBlank(response)
                ? decryptAuthorizationResponse(response)
                : new AuthorizationResponse(vpToken, error, errorDescription, state, false);
            val presentedState = authorizationResponse.state();
            require(StringUtils.isBlank(authorizationResponse.vpToken()) != StringUtils.isBlank(authorizationResponse.error())
                    && StringUtils.isNotBlank(presentedState),
                "Presentation response must carry a state and either a VP token or an error");
            val transientSessionTicket = configurationContext.getTicketRegistry().getTicket(presentedState, TransientSessionTicket.class);
            require(transientSessionTicket != null && !transientSessionTicket.isExpired(), "Presentation transaction is invalid");
            require(presentedState.equals(transientSessionTicket.getPropertyAsString("state")), "Presentation state does not match");
            require(authorizationResponse.encrypted() || StringUtils.isNotBlank(authorizationResponse.error())
                    || transientSessionTicket.getPropertyAsString(PROPERTY_RESPONSE_ENCRYPTION_KEY) == null,
                "Presentation transaction requires an encrypted response");

            if (StringUtils.isNotBlank(authorizationResponse.error())) {
                val outcome = new LinkedHashMap<String, Object>();
                outcome.put("status", STATUS_ERROR);
                outcome.put(OAuth20Constants.ERROR, StringUtils.truncate(authorizationResponse.error(), MAX_ERROR_LENGTH));
                if (StringUtils.isNotBlank(authorizationResponse.errorDescription())) {
                    outcome.put(OAuth20Constants.ERROR_DESCRIPTION,
                        StringUtils.truncate(authorizationResponse.errorDescription(), MAX_ERROR_DESCRIPTION_LENGTH));
                }
                return buildResponse(HttpStatus.OK, recordPresentationResult(transientSessionTicket, outcome,
                    resolvePresentationErrorId(transientSessionTicket.getId())));
            }

            val nonce = transientSessionTicket.getPropertyAsString("nonce");
            require(nonce != null && !nonce.isBlank(), "Presentation transaction has no nonce");
            val credentials = (List<CredentialRequest>) transientSessionTicket.getProperty("credentials", List.class);
            require(credentials != null && !credentials.isEmpty(), "Presentation transaction has no credential query");

            val disclosedClaims = validatePresentation(Objects.requireNonNull(authorizationResponse.vpToken()),
                credentials, nonce, transientSessionTicket);
            require(configurationContext.getTicketRegistry().deleteTicket(transientSessionTicket) > 0,
                "Presentation transaction was consumed concurrently");
            val walletResponse = new LinkedHashMap<String, Object>();
            walletResponse.put("status", STATUS_VERIFIED);
            walletResponse.putAll(recordPresentationResult(transientSessionTicket,
                Map.of("status", STATUS_VERIFIED, "claims", disclosedClaims), resolvePresentationResultId(transientSessionTicket.getId())));
            return buildResponse(HttpStatus.OK, walletResponse);
        } catch (final Throwable throwable) {
            LoggingUtils.warn(LOGGER, throwable);
            return buildResponse(HttpStatus.BAD_REQUEST,
                OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST,
                    "The presentation response could not be validated")
            );
        }
    }

    /**
     * Decrypt a {@code direct_post.jwt} response: an unsigned JWT encrypted with {@code ECDH-ES} to the
     * key generated for the request, whose response parameters are its top-level members (OpenID4VP 1.0 section 8.3).
     * The JWE {@code kid} names the request, which holds the key; the decrypted {@code state} must name the same request.
     * A {@code vp_token} given as a JSON object is handed on serialized, as it would arrive in the clear.
     *
     * @param response the JWE
     * @return the authorization response
     * @throws Exception the exception
     */
    protected AuthorizationResponse decryptAuthorizationResponse(final String response) throws Exception {
        val jwe = new JsonWebEncryption();
        jwe.setCompactSerialization(response);
        val keyId = jwe.getKeyIdHeaderValue();
        require(StringUtils.isNotBlank(keyId), "Encrypted presentation response names no key");
        val transaction = configurationContext.getTicketRegistry().getTicket(keyId, TransientSessionTicket.class);
        require(transaction != null && !transaction.isExpired(), "Presentation transaction is invalid");
        val encryptionKey = transaction.getPropertyAsString(PROPERTY_RESPONSE_ENCRYPTION_KEY);
        require(StringUtils.isNotBlank(encryptionKey), "Presentation transaction did not ask for an encrypted response");

        jwe.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
            KeyManagementAlgorithmIdentifiers.ECDH_ES));
        jwe.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
            RESPONSE_ENCRYPTION_ALGORITHMS_SUPPORTED.toArray(String[]::new)));
        jwe.setKey(PublicJsonWebKey.Factory.newPublicJwk(encryptionKey).getPrivateKey());
        val payload = MAPPER.readValue(jwe.getPayload(), Map.class);

        val state = Objects.toString(payload.get(OAuth20Constants.STATE), null);
        require(keyId.equals(state), "Encrypted presentation response does not match its request");
        val presentation = payload.get("vp_token");
        String vpToken = null;
        if (presentation instanceof final String value) {
            vpToken = value;
        } else if (presentation != null) {
            vpToken = MAPPER.writeValueAsString(presentation);
        }
        return new AuthorizationResponse(vpToken, Objects.toString(payload.get(OAuth20Constants.ERROR), null),
            Objects.toString(payload.get(OAuth20Constants.ERROR_DESCRIPTION), null), state, true);
    }

    /**
     * Record the outcome so that the relying party that created the request can collect it. The wallet
     * posts its presentation to this endpoint and is told whether it verified, but the relying party
     * is not party to that exchange at all; without a record it has no way to learn either the outcome
     * or the claims that were disclosed to it.
     * <p>
     * The record is bound to the client that created the request. When that client asked for a same-device
     * flow, a fresh response code is recorded with it and handed to the wallet inside the relying party's
     * {@code redirect_uri}, per OpenID4VP 1.0 section 8.2; the outcome is then only released against that code,
     * so it reaches the browser the wallet returned to rather than whoever holds the request id. A verified
     * outcome and an error are recorded under different identifiers, so an error can never replace a verified
     * outcome; a later error replaces an earlier one.
     *
     * @param transaction the presentation transaction
     * @param outcome     the outcome: a {@code status} of {@code verified} with the disclosed claims keyed by
     *                    credential query id, or of {@code error} with the wallet's {@code error} and
     *                    {@code error_description}
     * @param resultId    the identifier the outcome is recorded under
     * @return the parameters to return to the wallet, carrying {@code redirect_uri} for a same-device flow
     * @throws Exception the exception
     */
    protected Map<String, Object> recordPresentationResult(final TransientSessionTicket transaction,
                                                           final Map<String, Object> outcome,
                                                           final String resultId) throws Exception {
        val result = new LinkedHashMap<String, Object>(outcome);
        val clientId = transaction.getPropertyAsString(PROPERTY_CLIENT_ID);
        if (clientId != null) {
            result.put(PROPERTY_CLIENT_ID, clientId);
        }
        val walletResponse = new LinkedHashMap<String, Object>();
        val redirectUri = transaction.getPropertyAsString(PROPERTY_REDIRECT_URI);
        if (StringUtils.isNotBlank(redirectUri)) {
            val responseCode = RandomUtils.generateSecureRandomId();
            result.put(PROPERTY_RESPONSE_CODE, responseCode);
            walletResponse.put(OAuth20Constants.REDIRECT_URI, redirectUri + "#response_code=" + responseCode);
        }
        val factory = (TransientSessionTicketFactory) configurationContext.getTicketFactory().get(TransientSessionTicket.class);
        val resultTicket = factory.create(resultId, result);
        configurationContext.getTicketRegistry().deleteTicket(resultId);
        configurationContext.getTicketRegistry().addTicket(resultTicket);
        return walletResponse;
    }

    /**
     * Identifier under which the outcome of a presentation request is recorded.
     *
     * @param requestId the presentation request id
     * @return the result ticket id
     */
    public static String resolvePresentationResultId(final String requestId) {
        return TransientSessionTicketFactory.normalizeTicketId(requestId + "-result");
    }

    /**
     * Identifier under which a wallet's error response to a presentation request is recorded.
     *
     * @param requestId the presentation request id
     * @return the error ticket id
     */
    public static String resolvePresentationErrorId(final String requestId) {
        return TransientSessionTicketFactory.normalizeTicketId(requestId + "-error");
    }

    private Map<String, Map<String, Object>> validatePresentation(final String vpToken,
                                      final List<CredentialRequest> credentials,
                                      final String nonce,
                                      final TransientSessionTicket transientSessionTicket) throws Throwable {
        val credentialQueries = new LinkedHashMap<String, CredentialRequest>();
        for (val credential : credentials) {
            require(credential != null && credential.getId() != null && !credential.getId().isBlank(),
                "Credential query id is invalid");
            require(OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.DC_SD_JWT
                .getValue().equals(credential.getFormat()), "Credential query format is not supported");
            require(credential.getVctValues() != null && !credential.getVctValues().isEmpty()
                    && credential.getVctValues().stream().allMatch(value -> value != null && !value.isBlank()),
                "Credential query has no valid VCT values");
            require(credentialQueries.putIfAbsent(credential.getId(), credential) == null,
                "Credential query ids must be unique");
        }

        val disclosedClaims = new LinkedHashMap<String, Map<String, Object>>();
        val presentationResults = readJsonObject(vpToken);
        require(presentationResults.keySet().equals(credentialQueries.keySet()),
            "Presentation results do not satisfy the credential query");

        for (val entry : credentialQueries.entrySet()) {
            val presentations = presentationResults.get(entry.getKey());
            require(presentations instanceof final List<?> values && values.size() == 1,
                "Credential query must produce exactly one presentation");
            val presentation = ((List<?>) presentations).getFirst();
            require(presentation instanceof final String value && !value.isBlank(),
                "Credential presentation is invalid");
            disclosedClaims.put(entry.getKey(),
                validateSdJwtPresentation((String) presentation, entry.getValue(), nonce, transientSessionTicket));
        }
        return disclosedClaims;
    }

    private Map<String, Object> validateSdJwtPresentation(final String presentation,
                                           final CredentialRequest credentialQuery,
                                           final String nonce,
                                           final TransientSessionTicket transientSessionTicket) throws Throwable {
        val sdJwt = SDJWT.parse(presentation);
        require(sdJwt != null && "sha-256".equals(sdJwt.getHashAlgorithm()), "SD-JWT hash algorithm is not supported");
        require(sdJwt.getBindingJwt() != null && !sdJwt.getBindingJwt().isBlank(),
            "Credential presentation has no key binding JWT");

        val credentialJwt = parseSignedJwt(sdJwt.getCredentialJwt());
        require(credentialJwt.getHeader().getType() != null
                && CREDENTIAL_JWT_TYPES.contains(credentialJwt.getHeader().getType().toString()),
            "Credential JWT type is invalid");
        val encodedClaims = readJwtClaims(sdJwt.getCredentialJwt());
        val issuer = requiredStringClaim(encodedClaims, "iss");
        val configuredIssuer = configurationContext.getCasProperties().getAuthn().getOidc().getCore().getIssuer();
        require(configuredIssuer.equals(issuer), "Credential issuer is not trusted");

        val credentialType = requiredStringClaim(encodedClaims, "vct");
        require(credentialQuery.getVctValues().contains(credentialType), "Credential type does not satisfy the query");
        val credentialConfiguration = resolveCredentialConfiguration(credentialType, configuredIssuer);
        val algorithm = credentialJwt.getHeader().getAlgorithm();
        require(algorithm != null && credentialConfiguration.configuration().getCredentialSigningAlgValuesSupported()
            .contains(algorithm.getName()), "Credential JWT algorithm is not allowed");

        val configurationId = encodedClaims.get("credential_configuration_id");
        require(configurationId == null || credentialConfiguration.id().equals(configurationId),
            "Credential configuration does not match its type");
        verifyCredentialSignature(credentialJwt);
        require(encodedClaims.containsKey("exp"), "Credential has no expiration time");
        validateTimeClaims(encodedClaims, true, null);
        require(!encodedClaims.containsKey("status"), "Credential status validation is not supported");

        val cnf = encodedClaims.get("cnf");
        require(cnf instanceof Map<?, ?>, "Credential has no holder binding key");
        val holderJwkValue = ((Map<?, ?>) cnf).get("jwk");
        require(holderJwkValue instanceof Map<?, ?>, "Credential holder binding key is invalid");
        val holderJwk = parsePublicJwk((Map<?, ?>) holderJwkValue);

        val disclosedClaims = decodeDisclosures(encodedClaims, sdJwt.getDisclosures(), sdJwt.getHashAlgorithm());
        validateRequestedClaims(disclosedClaims, credentialQuery.getClaims());
        val expectedAudience = OidcVerifiableCredentialPresentationRequestEndpointController
            .resolveClientIdentifier(configurationContext);
        validateKeyBindingJwt(sdJwt, holderJwk, nonce, expectedAudience, transientSessionTicket);
        return disclosedClaims;
    }

    private CredentialConfiguration resolveCredentialConfiguration(final String credentialType, final String issuer) {
        val configurations = configurationContext.getCasProperties().getAuthn().getOidc().getVc()
            .getIssuer().getCredentialConfigurations();
        return configurations.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getFormat()
                == OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.DC_SD_JWT)
            .filter(entry -> credentialType.equals(
                issuer + '/' + OidcConstants.VC_CREDENTIAL_TYPE_URL + '/' + entry.getKey()))
            .map(entry -> new CredentialConfiguration(entry.getKey(), entry.getValue()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Credential type is not configured"));
    }

    /**
     * Verify the credential against the issuer key its {@code kid} names. Credentials do not say which client
     * they were issued to, which would let every verifier correlate the holder with that relying party, so the
     * key is looked up in CAS's signing keystore by key id, the same way a third-party verifier finds it through
     * the JWT VC Issuer Metadata. A credential without a {@code kid} is checked against the default signing key.
     *
     * @param credentialJwt the issuer-signed credential JWT
     * @throws Throwable the throwable
     */
    private void verifyCredentialSignature(final SignedJWT credentialJwt) throws Throwable {
        val keySelector = new OidcRegisteredService();
        keySelector.setJwksKeyId(credentialJwt.getHeader().getKeyID());
        val signingKey = configurationContext.getIdTokenSigningAndEncryptionService()
            .getJsonWebKeySigningKey(Optional.of(keySelector));
        require(signingKey != null && signingKey.getPublicKey() != null, "Credential issuer has no signing key");
        val jwk = JWK.parse(signingKey.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
        require(verify(credentialJwt, jwk), "Credential JWT signature is invalid");
    }

    private static void validateKeyBindingJwt(final SDJWT sdJwt,
                                              final JWK holderJwk,
                                              final String nonce,
                                              final String expectedAudience,
                                              final TransientSessionTicket transientSessionTicket) throws Exception {
        val bindingJwt = parseSignedJwt(sdJwt.getBindingJwt());
        require(bindingJwt.getHeader().getType() != null
                && "kb+jwt".equals(bindingJwt.getHeader().getType().toString()),
            "Key binding JWT type is invalid");
        val algorithm = bindingJwt.getHeader().getAlgorithm();
        require(algorithm != null && (KEY_BINDING_ALGORITHMS_SUPPORTED.contains(algorithm.getName())
                || EDWARDS_CURVE_ALGORITHMS.contains(algorithm.getName())),
            "Key binding JWT algorithm is not allowed");
        require(verify(bindingJwt, holderJwk), "Key binding JWT signature is invalid");

        val claims = readJwtClaims(sdJwt.getBindingJwt());
        require(nonce.equals(requiredStringClaim(claims, "nonce")), "Key binding nonce does not match");
        require(constantTimeEquals(sdJwt.getSDHash(), requiredStringClaim(claims, "sd_hash")),
            "Key binding SD-JWT hash does not match");

        require(readAudience(claims).equals(List.of(expectedAudience)), "Key binding audience does not match");

        val creationTime = transientSessionTicket.getCreationTime();
        require(creationTime != null, "Presentation transaction has no creation time");
        validateTimeClaims(claims, true, creationTime.toInstant());
    }

    private static Map<String, Object> decodeDisclosures(final Map<String, Object> encodedClaims,
                                                         final List<Disclosure> disclosures,
                                                         final String hashAlgorithm) throws Exception {
        val disclosuresByDigest = new LinkedHashMap<String, Disclosure>();
        val disclosureValues = new HashSet<String>();
        for (val disclosure : disclosures) {
            require(disclosure != null && disclosureValues.add(disclosure.getDisclosure()),
                "Duplicate disclosure is invalid");
            MAPPER.readValue(disclosure.getJson(), List.class);
            require(disclosuresByDigest.putIfAbsent(disclosure.digest(hashAlgorithm), disclosure) == null,
                "Duplicate disclosure digest is invalid");
        }

        val encounteredDigests = new HashSet<String>();
        val usedDisclosures = new HashSet<String>();
        val decoded = decodeMap(encodedClaims, disclosuresByDigest, encounteredDigests, usedDisclosures, true, 0);
        require(usedDisclosures.equals(disclosuresByDigest.keySet()), "Presentation contains an unreferenced disclosure");
        return decoded;
    }

    private static Map<String, Object> decodeMap(final Map<?, ?> encoded,
                                                 final Map<String, Disclosure> disclosures,
                                                 final Set<String> encounteredDigests,
                                                 final Set<String> usedDisclosures,
                                                 final boolean root,
                                                 final int depth) {
        require(depth <= MAX_DISCLOSURE_DEPTH, "SD-JWT disclosure nesting is too deep");
        val decoded = new LinkedHashMap<String, Object>();
        for (val entry : encoded.entrySet()) {
            require(entry.getKey() instanceof String, "SD-JWT object key is invalid");
            val key = (String) entry.getKey();
            if ("_sd_alg".equals(key)) {
                require(root && "sha-256".equals(entry.getValue()), "SD-JWT hash algorithm is invalid");
            } else if ("_sd".equals(key)) {
                require(entry.getValue() instanceof List<?>, "SD-JWT disclosure digest list is invalid");
                for (val value : (List<?>) entry.getValue()) {
                    require(value instanceof String, "SD-JWT disclosure digest is invalid");
                    val digest = (String) value;
                    require(encounteredDigests.add(digest), "SD-JWT disclosure digest is duplicated");
                    val disclosure = disclosures.get(digest);
                    if (disclosure != null) {
                        val claimName = disclosure.getClaimName();
                        require(claimName != null && !encoded.containsKey(claimName) && !decoded.containsKey(claimName),
                            "SD-JWT disclosed claim is invalid");
                        usedDisclosures.add(digest);
                        decoded.put(claimName, decodeValue(disclosure.getClaimValue(), disclosures,
                            encounteredDigests, usedDisclosures, depth + 1));
                    }
                }
            } else {
                require(!"...".equals(key), "SD-JWT array digest is misplaced");
                decoded.put(key, decodeValue(entry.getValue(), disclosures,
                    encounteredDigests, usedDisclosures, depth + 1));
            }
        }
        return decoded;
    }

    private static List<Object> decodeList(final List<?> encoded,
                                           final Map<String, Disclosure> disclosures,
                                           final Set<String> encounteredDigests,
                                           final Set<String> usedDisclosures,
                                           final int depth) {
        require(depth <= MAX_DISCLOSURE_DEPTH, "SD-JWT disclosure nesting is too deep");
        val decoded = new ArrayList<>();
        for (val value : encoded) {
            if (value instanceof final Map<?, ?> map && map.containsKey("...")) {
                require(map.size() == 1 && map.get("...") instanceof String, "SD-JWT array digest is invalid");
                val digest = (String) map.get("...");
                require(encounteredDigests.add(digest), "SD-JWT disclosure digest is duplicated");
                val disclosure = disclosures.get(digest);
                if (disclosure != null) {
                    require(disclosure.getClaimName() == null, "SD-JWT array disclosure is invalid");
                    usedDisclosures.add(digest);
                    decoded.add(decodeValue(disclosure.getClaimValue(), disclosures,
                        encounteredDigests, usedDisclosures, depth + 1));
                }
            } else {
                decoded.add(decodeValue(value, disclosures, encounteredDigests, usedDisclosures, depth + 1));
            }
        }
        return decoded;
    }

    private static Object decodeValue(final Object value,
                                      final Map<String, Disclosure> disclosures,
                                      final Set<String> encounteredDigests,
                                      final Set<String> usedDisclosures,
                                      final int depth) {
        return switch (value) {
            case final Map<?, ?> map -> decodeMap(map, disclosures, encounteredDigests, usedDisclosures, false, depth);
            case final List<?> list -> decodeList(list, disclosures, encounteredDigests, usedDisclosures, depth);
            default -> value;
        };
    }

    /**
     * Check the disclosed claims against the claim query. Required claims must all be disclosed. Claims marked
     * as not required are requested through DCQL {@code claim_sets}, so the wallet may withhold them; when every
     * claim is optional, at least one of them must be disclosed, since each is offered as an option of its own.
     *
     * @param disclosedClaims the disclosed claims
     * @param claimRequests   the claim query
     */
    private static void validateRequestedClaims(final Map<String, Object> disclosedClaims,
                                                final @Nullable List<ClaimRequest> claimRequests) {
        if (claimRequests == null || claimRequests.isEmpty()) {
            return;
        }
        for (val claimRequest : claimRequests) {
            require(claimRequest != null && claimRequest.getPath() != null && !claimRequest.getPath().isEmpty()
                    && claimRequest.getPath().stream().allMatch(path -> path != null && !path.isBlank()),
                "Credential claim query is invalid");
        }
        val requiredClaims = claimRequests.stream().filter(ClaimRequest::isRequired).toList();
        if (requiredClaims.isEmpty()) {
            require(claimRequests.stream().anyMatch(claimRequest -> hasClaimPath(disclosedClaims, claimRequest.getPath())),
                "Credential does not contain any of the requested claims");
        } else {
            for (val claimRequest : requiredClaims) {
                require(hasClaimPath(disclosedClaims, claimRequest.getPath()),
                    "Credential does not contain a requested claim");
            }
        }
    }

    private static boolean hasClaimPath(final Map<String, Object> claims, final List<String> path) {
        var current = (Object) claims;
        for (val segment : path) {
            if (!(current instanceof final Map<?, ?> map) || !map.containsKey(segment)) {
                return false;
            }
            current = map.get(segment);
        }
        return true;
    }

    private static SignedJWT parseSignedJwt(final String jwt) throws Exception {
        require(jwt != null && !jwt.isBlank(), "Signed JWT is missing");
        readJwtPart(jwt, 0);
        readJwtPart(jwt, 1);
        val signedJwt = SignedJWT.parse(jwt);
        require(signedJwt.getState() != null, "Signed JWT is invalid");
        return signedJwt;
    }

    private static Map<String, Object> readJwtClaims(final String jwt) throws Exception {
        return readJwtPart(jwt, 1);
    }

    private static Map<String, Object> readJwtPart(final String jwt, final int part) throws Exception {
        val parts = jwt.split("\\.", -1);
        require(parts.length == 3 && !parts[part].isBlank(), "Signed JWT compact form is invalid");
        return readJsonObject(Base64.getUrlDecoder().decode(parts[part]));
    }

    private static Map<String, Object> readJsonObject(final String value) throws Exception {
        return readJsonObject(value.getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, Object> readJsonObject(final byte[] value) throws Exception {
        val result = MAPPER.readValue(value, Map.class);
        require(result != null, "JSON object is invalid");
        return result;
    }

    private static JWK parsePublicJwk(final Map<?, ?> value) throws Exception {
        val jwkValues = new LinkedHashMap<String, Object>();
        value.forEach((key, entryValue) -> {
            require(key instanceof String, "JWK member name is invalid");
            jwkValues.put((String) key, entryValue);
        });
        val jwk = JWK.parse(jwkValues);
        require(!jwk.isPrivate(), "Holder JWK must not contain private key material");
        require(jwk instanceof ECKey || jwk instanceof RSAKey || jwk instanceof OctetKeyPair,
            "Holder JWK type is not supported");
        return jwk.toPublicJWK();
    }

    private static boolean verify(final SignedJWT signedJwt, final JWK jwk) throws Exception {
        return switch (jwk) {
            case final ECKey ecKey -> signedJwt.verify(new ECDSAVerifier(ecKey.toPublicJWK()));
            case final RSAKey rsaKey -> signedJwt.verify(new RSASSAVerifier(rsaKey.toPublicJWK()));
            case final OctetKeyPair octetKeyPair -> verifyEdwardsCurveSignature(signedJwt, octetKeyPair);
            default -> throw new IllegalArgumentException("JWK type is not supported");
        };
    }

    /**
     * Verify an Ed25519 signature with the JDK. Both the fully specified {@code Ed25519} and the older
     * {@code EdDSA} header values are accepted; neither Nimbus (which needs Google Tink) nor jose4j (which only
     * knows {@code EdDSA}) covers both.
     *
     * @param signedJwt the signed JWT
     * @param jwk       the Ed25519 public key
     * @return true if the signature verifies
     * @throws Exception the exception
     */
    private static boolean verifyEdwardsCurveSignature(final SignedJWT signedJwt, final OctetKeyPair jwk) throws Exception {
        val algorithm = signedJwt.getHeader().getAlgorithm();
        if (algorithm == null || !EDWARDS_CURVE_ALGORITHMS.contains(algorithm.getName())) {
            return false;
        }
        val publicKey = (PublicKey) EncodingUtils.newJsonWebKey(jwk.toPublicJWK().toJSONString()).getKey();
        val signature = java.security.Signature.getInstance("Ed25519");
        signature.initVerify(publicKey);
        signature.update(signedJwt.getSigningInput());
        return signature.verify(signedJwt.getSignature().decode());
    }

    private static void validateTimeClaims(final Map<String, Object> claims,
                                           final boolean issuedAtRequired,
                                           @Nullable final Instant earliestIssuedAt) {
        val now = Instant.now(Clock.systemUTC());
        val expirationTime = readNumericDate(claims, "exp", false);
        require(expirationTime == null || now.minus(CLOCK_SKEW).isBefore(expirationTime), "JWT has expired");
        val notBefore = readNumericDate(claims, "nbf", false);
        require(notBefore == null || !now.plus(CLOCK_SKEW).isBefore(notBefore), "JWT is not yet valid");
        val issuedAt = readNumericDate(claims, "iat", issuedAtRequired);
        require(issuedAt == null || !now.plus(CLOCK_SKEW).isBefore(issuedAt), "JWT issue time is in the future");
        require(earliestIssuedAt == null || (issuedAt != null
                && !issuedAt.isBefore(earliestIssuedAt.minus(CLOCK_SKEW))),
            "Key binding JWT predates the presentation transaction");
    }

    private static @Nullable Instant readNumericDate(final Map<String, Object> claims,
                                                     final String name,
                                                     final boolean required) {
        val value = claims.get(name);
        require(value != null || !required, "JWT is missing a required time claim");
        if (value == null) {
            return null;
        }
        require(value instanceof Number, "JWT time claim is invalid");
        try {
            val numericDate = new BigDecimal(value.toString());
            val components = numericDate.divideAndRemainder(BigDecimal.ONE);
            return Instant.ofEpochSecond(components[0].longValueExact(),
                components[1].movePointRight(9).longValueExact());
        } catch (final ArithmeticException exception) {
            throw new IllegalArgumentException("JWT time claim is invalid", exception);
        }
    }

    private static List<String> readAudience(final Map<String, Object> claims) {
        val audience = claims.get("aud");
        if (audience instanceof final String value) {
            return List.of(value);
        }
        if (audience instanceof final List<?> values
            && values.stream().allMatch(String.class::isInstance)) {
            return values.stream().map(String.class::cast).toList();
        }
        throw new IllegalArgumentException("JWT audience is invalid");
    }

    private static String requiredStringClaim(final Map<String, Object> claims, final String name) {
        val value = claims.get(name);
        if (!(value instanceof final String stringValue) || stringValue.isBlank()) {
            throw new IllegalArgumentException("JWT string claim is missing or invalid");
        }
        return stringValue;
    }

    private static boolean constantTimeEquals(final String left, final String right) {
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    private static ResponseEntity<Map<String, Object>> buildResponse(final HttpStatus status,
                                                                     final Map<String, Object> body) {
        return ResponseEntity.status(status)
            .cacheControl(CacheControl.noStore())
            .header(HttpHeaders.PRAGMA, "no-cache")
            .body(body);
    }

    private static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private record CredentialConfiguration(String id,
        OidcVerifiableCredentialConfigurationProperties configuration) {
    }

    /**
     * Authorization response parameters, as posted in the clear or decrypted from a {@code direct_post.jwt} response.
     *
     * @param vpToken          the vp token
     * @param error            the error code
     * @param errorDescription the error description
     * @param state            the state
     * @param encrypted        whether the response was encrypted
     */
    protected record AuthorizationResponse(@Nullable String vpToken, @Nullable String error, @Nullable String errorDescription,
                                           @Nullable String state, boolean encrypted) {
    }
}
