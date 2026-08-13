package org.apereo.cas.vc.presentation;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.ClaimRequest;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.CredentialRequest;
import com.authlete.sd.Disclosure;
import com.authlete.sd.SDJWT;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.Ed25519Verifier;
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
import org.jose4j.jwk.JsonWebKey;
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
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false)
        .minimal(true)
        .jsonFactory(JsonFactory.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .build())
        .build()
        .toObjectMapper();

    private static final Set<String> CREDENTIAL_JWT_TYPES = Set.of("dc+sd-jwt", "vc+sd-jwt");

    private static final Set<String> KEY_BINDING_ALGORITHMS = Set.of("ES256", "ES384", "ES512");

    private static final Duration CLOCK_SKEW = Duration.ofSeconds(30);

    private static final int MAX_DISCLOSURE_DEPTH = 64;

    public OidcVerifiableCredentialPresentationResponseEndpointController(
        final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    /**
     * Handle response response entity.
     *
     * @param vpToken the vp token
     * @param state   the state
     * @return the response entity
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL,
        "/**/" + OidcConstants.VC_PRESENTATION_RESPONSE_URL
    }, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle response for presentation request", parameters = {
        @Parameter(name = "vp_token", description = "The verifiable presentation token"),
        @Parameter(name = "state", description = "The state parameter returned from the request")
    })
    public ResponseEntity<Map<String, Object>> handleResponse(
        @RequestParam("vp_token") final String vpToken,
        @RequestParam final String state) {

        try {
            require(!vpToken.isBlank() && !state.isBlank(), "Presentation response parameters cannot be blank");
            val transientSessionTicket = configurationContext.getTicketRegistry().getTicket(state, TransientSessionTicket.class);
            require(transientSessionTicket != null && !transientSessionTicket.isExpired(), "Presentation transaction is invalid");
            require(state.equals(transientSessionTicket.getPropertyAsString("state")), "Presentation state does not match");

            val nonce = transientSessionTicket.getPropertyAsString("nonce");
            require(nonce != null && !nonce.isBlank(), "Presentation transaction has no nonce");
            val credentials = (List<CredentialRequest>) transientSessionTicket.getProperty("credentials", List.class);
            require(credentials != null && !credentials.isEmpty(), "Presentation transaction has no credential query");

            validatePresentation(vpToken, credentials, nonce, transientSessionTicket);
            configurationContext.getTicketRegistry().deleteTicket(transientSessionTicket);
            
            return buildResponse(HttpStatus.OK, Map.of("status", "verified"));
        } catch (final Throwable throwable) {
            LoggingUtils.warn(LOGGER, throwable);
            return buildResponse(HttpStatus.BAD_REQUEST,
                OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST,
                    "The presentation response could not be validated")
            );
        }
    }

    private void validatePresentation(final String vpToken,
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
            validateSdJwtPresentation((String) presentation, entry.getValue(), nonce, transientSessionTicket);
        }
    }

    private void validateSdJwtPresentation(final String presentation,
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
        val clientId = requiredStringClaim(encodedClaims, "client_id");
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(), clientId);
        require(registeredService != null, "Credential client is not registered");
        verifyCredentialSignature(credentialJwt, registeredService);
        validateTimeClaims(encodedClaims, false, null);
        require(!encodedClaims.containsKey("status"), "Credential status validation is not supported");

        val cnf = encodedClaims.get("cnf");
        require(cnf instanceof Map<?, ?>, "Credential has no holder binding key");
        val holderJwkValue = ((Map<?, ?>) cnf).get("jwk");
        require(holderJwkValue instanceof Map<?, ?>, "Credential holder binding key is invalid");
        val holderJwk = parsePublicJwk((Map<?, ?>) holderJwkValue);

        val disclosedClaims = decodeDisclosures(encodedClaims, sdJwt.getDisclosures(), sdJwt.getHashAlgorithm());
        validateRequestedClaims(disclosedClaims, credentialQuery.getClaims());
        val expectedAudience = "redirect_uri:" + configuredIssuer + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;
        validateKeyBindingJwt(sdJwt, holderJwk, nonce, expectedAudience, transientSessionTicket);
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

    private void verifyCredentialSignature(final SignedJWT credentialJwt,
                                           final OAuthRegisteredService registeredService) throws Throwable {
        val signingKey = configurationContext.getIdTokenSigningAndEncryptionService()
            .getJsonWebKeySigningKey(Optional.of(registeredService));
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
        require(algorithm != null && KEY_BINDING_ALGORITHMS.contains(algorithm.getName()),
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

    private static void validateRequestedClaims(final Map<String, Object> disclosedClaims,
                                                final List<ClaimRequest> claimRequests) {
        if (claimRequests == null) {
            return;
        }
        for (val claimRequest : claimRequests) {
            require(claimRequest != null && claimRequest.getPath() != null && !claimRequest.getPath().isEmpty()
                    && claimRequest.getPath().stream().allMatch(path -> path != null && !path.isBlank()),
                "Credential claim query is invalid");
            require(hasClaimPath(disclosedClaims, claimRequest.getPath()),
                "Credential does not contain a requested claim");
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
        val verifier = switch (jwk) {
            case final ECKey ecKey -> new ECDSAVerifier(ecKey.toPublicJWK());
            case final RSAKey rsaKey -> new RSASSAVerifier(rsaKey.toPublicJWK());
            case final OctetKeyPair octetKeyPair -> new Ed25519Verifier(octetKeyPair.toPublicJWK());
            default -> throw new IllegalArgumentException("JWK type is not supported");
        };
        return signedJwt.verify((JWSVerifier) verifier);
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
}
