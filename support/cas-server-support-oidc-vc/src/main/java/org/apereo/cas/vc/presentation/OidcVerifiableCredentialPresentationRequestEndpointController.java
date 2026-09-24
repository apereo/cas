package org.apereo.cas.vc.presentation;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialsPresentationProperties.ClientIdentifierPrefixes;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jspecify.annotations.Nullable;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.security.cert.X509Certificate;

/**
 * This is {@link OidcVerifiableCredentialPresentationRequestEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialPresentationRequestEndpointController extends BaseOAuth20Controller<OidcConfigurationContext> {
    /**
     * Media type of a request object served by reference, per OpenID4VP 1.0 section 5.10.
     * It is set on the response rather than declared as a mapping's produces condition, so that
     * a wallet sending a narrower Accept header is still served instead of being refused.
     */
    private static final MediaType REQUEST_OBJECT_MEDIA_TYPE = MediaType.parseMediaType("application/oauth-authz-req+jwt");

    /**
     * Type header every request object must carry, per RFC 9101. Wallets reject request
     * objects that do not declare it.
     */
    private static final String REQUEST_OBJECT_JWT_TYPE = "oauth-authz-req+jwt";

    /**
     * Audience of a request object when the wallet was not discovered dynamically,
     * per OpenID4VP 1.0.
     */
    private static final String STATIC_DISCOVERY_AUDIENCE = "https://self-issued.me/v2";

    private static final int SUBJECT_ALTERNATIVE_NAME_DNS = 2;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(true).build().toObjectMapper();

    public OidcVerifiableCredentialPresentationRequestEndpointController(
        final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    /**
     * The client identifier CAS presents to wallets as the verifier, carrying the OpenID4VP
     * client identifier prefix that tells the wallet how to authenticate it. The wallet echoes
     * this value as the key binding audience, so the presentation response endpoint resolves
     * the expected audience through this same method.
     *
     * @param casProperties the CAS properties
     * @return the client identifier
     */
    public static String resolveClientIdentifier(final CasConfigurationProperties casProperties) {
        val responseUri = resolveResponseUri(casProperties);
        val prefix = casProperties.getAuthn().getOidc().getVc().getPresentation().getClientIdentifierPrefix();
        val identifier = prefix == ClientIdentifierPrefixes.X509_SAN_DNS
            ? URI.create(responseUri).getHost()
            : responseUri;
        return prefix.getValue() + ':' + identifier;
    }

    /**
     * Resolve response uri.
     *
     * @param casProperties the CAS properties
     * @return the response uri
     */
    public static String resolveResponseUri(final CasConfigurationProperties casProperties) {
        return casProperties.getAuthn().getOidc().getCore().getIssuer()
            + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;
    }

    /**
     * Serve the authorization request object by reference.
     * <p>
     * OpenID4VP 1.0 requires the request URI response to be a signed request object served as
     * {@code application/oauth-authz-req+jwt}. A request cannot be signed under the
     * {@code redirect_uri} client identifier prefix, because the wallet has no way to obtain a
     * trusted key for it; in that mode CAS never advertises a request URI and this endpoint
     * has nothing to serve.
     *
     * @param requestId    the request id
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     * @throws Throwable the throwable
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL + "/{requestId}",
        "/**/" + OidcConstants.VC_PRESENTATION_REQUEST_URL + "/{requestId}"
    })
    @Operation(summary = "Fetch an OIDC verifiable credential presentation request object",
        description = "Serves the signed authorization request object for a presentation request")
    public ResponseEntity<String> fetchRequest(
        @PathVariable final String requestId,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) throws Throwable {

        if (!isRequestObjectSigned()) {
            LOGGER.warn("Presentation requests are not signed and are passed to the wallet by value; "
                + "no request object can be served by reference under the [{}] client identifier prefix.",
                ClientIdentifierPrefixes.REDIRECT_URI.getValue());
            return ResponseEntity.notFound().build();
        }

        val transientSessionTicket = configurationContext.getTicketRegistry().getTicket(requestId, TransientSessionTicket.class);
        Objects.requireNonNull(transientSessionTicket, () -> "No request could be found for the given request id: " + requestId);

        val requestObject = signAuthorizationRequest(
            buildAuthorizationRequestParameters(transientSessionTicket), transientSessionTicket);
        return ResponseEntity
            .ok()
            .contentType(REQUEST_OBJECT_MEDIA_TYPE)
            .cacheControl(CacheControl.noStore())
            .body(requestObject);
    }

    /**
     * Collect the outcome of a presentation request.
     * <p>
     * The wallet posts its presentation to the response endpoint and is told there whether it verified.
     * The relying party that created the request is not party to that exchange, so this is where it
     * learns the outcome and the claims that were disclosed to it. A request that has not been answered
     * yet reports {@code pending}; one that was answered and collected, or that expired, is gone.
     *
     * @param requestId    the request id returned when the presentation request was created
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_RESULT_URL,
        "/**/" + OidcConstants.VC_PRESENTATION_RESULT_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Collect the outcome of an OIDC verifiable credential presentation request",
        parameters = @Parameter(name = "requestId", description = "The presentation request id"))
    public ResponseEntity<Map<String, Object>> fetchResult(
        @RequestParam("requestId") final String requestId,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) {

        val resultId = OidcVerifiableCredentialPresentationResponseEndpointController.resolvePresentationResultId(requestId);
        val result = FunctionUtils.doAndHandle(
            () -> configurationContext.getTicketRegistry().getTicket(resultId, TransientSessionTicket.class));
        if (result != null && !result.isExpired()) {
            val body = Map.<String, Object>of(
                "status", Objects.requireNonNull(result.getPropertyAsString("status")),
                "claims", Objects.requireNonNullElseGet(result.getProperty("claims", Map.class), Map::of));
            FunctionUtils.doAndHandle(_ -> configurationContext.getTicketRegistry().deleteTicket(result));
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
        }
        val pending = FunctionUtils.doAndHandle(
            () -> configurationContext.getTicketRegistry().getTicket(requestId, TransientSessionTicket.class));
        return pending != null && !pending.isExpired()
            ? ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("status", "pending"))
            : ResponseEntity.notFound().build();
    }

    /**
     * Handle and create response entity.
     *
     * @param request      the request
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     * @throws Throwable the throwable
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL,
        "/**/" + OidcConstants.VC_PRESENTATION_REQUEST_URL
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC verifiable credential presentation request",
        description = "Handles requests for OIDC verifiable credential presentation issuance")
    public ResponseEntity handle(
        @Valid @RequestBody final OidcVerifiableCredentialPresentationRequest request,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) throws Throwable {

        val factory = (TransientSessionTicketFactory) configurationContext.getTicketFactory().get(TransientSessionTicket.class);
        val transientSessionTicket = factory.create(CollectionUtils.wrap(
            "nonce", UUID.randomUUID().toString(),
            "credentials", request.getCredentials()
        ));
        transientSessionTicket.putProperty("state", transientSessionTicket.getId());
        val addedTicket = (TransientSessionTicket) configurationContext.getTicketRegistry().addTicket(transientSessionTicket);

        val parameters = buildAuthorizationRequestParameters(addedTicket);
        final String requestUri = isRequestObjectSigned()
            ? configurationContext.getCasProperties().getAuthn().getOidc().getCore().getIssuer()
              + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL + '/' + addedTicket.getId()
            : null;
        val authorizationRequest = requestUri != null
            ? buildDeepLink(Map.of(
                OAuth20Constants.CLIENT_ID, parameters.get(OAuth20Constants.CLIENT_ID),
                OidcConstants.REQUEST_URI, requestUri))
            : buildDeepLink(parameters);

        return ResponseEntity.ok(
            OidcVerifiableCredentialPresentationResponse
                .builder()
                .requestId(addedTicket.getId())
                .requestUri(requestUri)
                .authorizationRequest(authorizationRequest)
                .expiresIn(transientSessionTicket.getExpirationPolicy().getTimeToLive())
                .build()
        );
    }

    protected boolean isRequestObjectSigned() {
        return configurationContext.getCasProperties().getAuthn().getOidc().getVc()
            .getPresentation().getClientIdentifierPrefix() != ClientIdentifierPrefixes.REDIRECT_URI;
    }

    /**
     * Build the OpenID4VP authorization request parameters for a presentation transaction.
     * The same parameters are carried by value in the deep link or signed into a request
     * object, so that both delivery modes describe an identical request.
     *
     * @param transientSessionTicket the presentation transaction
     * @return the authorization request parameters
     */
    protected Map<String, Object> buildAuthorizationRequestParameters(final TransientSessionTicket transientSessionTicket) {
        val casProperties = configurationContext.getCasProperties();
        val clientMetadata = OidcVerifiableCredentialPresentationClientMetadata.builder()
            .clientName("Apereo CAS")
            .vpFormatsSupported(Map.of(
                OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.DC_SD_JWT.getValue(),
                OidcVerifiableCredentialPresentationClientMetadata.VpFormat.builder()
                    .algValues(List.of("ES256", "ES384", "ES512"))
                    .sdJwtAlgValues(List.of("ES256", "ES384", "ES512"))
                    .kbJwtAlgValues(List.of("ES256", "ES384", "ES512"))
                    .build()
            ))
            .build();

        val credentials = (List<OidcVerifiableCredentialPresentationRequest.CredentialRequest>)
            Objects.requireNonNull(transientSessionTicket.getProperty("credentials", List.class));
        val dcqlCredentials = credentials
            .stream()
            .map(credential -> OidcVerifiableCredentialDCQL
                .builder()
                .id(credential.getId())
                .format(credential.getFormat())
                .meta(OidcVerifiableCredentialDCQL.Meta.builder()
                    .vctValues(credential.getVctValues())
                    .build())
                .claims(credential.getClaims()
                    .stream()
                    .<OidcVerifiableCredentialDCQL.DCQLCredentialClaimRequest>map(claim ->
                        OidcVerifiableCredentialDCQL.DCQLCredentialClaimRequest.builder()
                            .path(claim.getPath()).build())
                    .toList()
                )
                .build())
            .toList();

        val parameters = new LinkedHashMap<String, Object>();
        parameters.put(OAuth20Constants.CLIENT_ID, resolveClientIdentifier(casProperties));
        parameters.put(OAuth20Constants.RESPONSE_TYPE, "vp_token");
        parameters.put(OAuth20Constants.RESPONSE_MODE, "direct_post");
        parameters.put("response_uri", resolveResponseUri(casProperties));
        parameters.put(OAuth20Constants.NONCE, Objects.requireNonNull(transientSessionTicket.getPropertyAsString("nonce")));
        parameters.put(OAuth20Constants.STATE, Objects.requireNonNull(transientSessionTicket.getPropertyAsString("state")));
        parameters.put("dcql_query", MAPPER.convertValue(Map.of("credentials", dcqlCredentials), Map.class));
        parameters.put("client_metadata", MAPPER.convertValue(clientMetadata, Map.class));
        return parameters;
    }

    /**
     * Sign the authorization request into a request object.
     * The {@code typ} header and the audience are what let a wallet accept the object at all.
     *
     * @param parameters             the authorization request parameters
     * @param transientSessionTicket the presentation transaction
     * @return the compact serialization of the request object
     * @throws Throwable the throwable
     */
    protected String signAuthorizationRequest(final Map<String, Object> parameters,
                                              final TransientSessionTicket transientSessionTicket) throws Throwable {
        val signingKey = configurationContext.getIdTokenSigningAndEncryptionService()
            .getJsonWebKeySigningKey(Optional.empty());
        Objects.requireNonNull(signingKey, "CAS has no OpenID Connect signing key to sign the request object");
        Objects.requireNonNull(signingKey.getPrivateKey(), "The OpenID Connect signing key has no private key");

        val clientId = parameters.get(OAuth20Constants.CLIENT_ID).toString();
        val certificateChain = verifyCertificateChain(signingKey, StringUtils.substringAfter(clientId, ":"));

        val claims = new JwtClaims();
        parameters.forEach(claims::setClaim);
        claims.setIssuer(clientId);
        claims.setAudience(STATIC_DISCOVERY_AUDIENCE);
        val issuedAt = NumericDate.now();
        claims.setIssuedAt(issuedAt);
        claims.setExpirationTime(NumericDate.fromSeconds(
            issuedAt.getValue() + transientSessionTicket.getExpirationPolicy().getTimeToLive()));

        val jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(signingKey.getPrivateKey());
        FunctionUtils.doIfNotNull(signingKey.getKeyId(), jws::setKeyIdHeaderValue);
        jws.setAlgorithmHeaderValue(signingKey instanceof EllipticCurveJsonWebKey
            ? AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256
            : AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setHeader("typ", REQUEST_OBJECT_JWT_TYPE);
        jws.setCertificateChainHeaderValue(certificateChain.toArray(X509Certificate[]::new));
        return jws.getCompactSerialization();
    }

    /**
     * A wallet authenticates an {@code x509_san_dns} verifier by matching the client identifier
     * against a DNS subject alternative name in the leaf certificate. Failing here produces a
     * configuration error rather than a request object every wallet silently rejects.
     *
     * @param signingKey the signing key
     * @param dnsName    the DNS name taken from the client identifier
     * @return the certificate chain
     * @throws Exception the exception
     */
    protected List<X509Certificate> verifyCertificateChain(final PublicJsonWebKey signingKey,
                                                           final String dnsName) throws Exception {
        val certificateChain = signingKey.getCertificateChain();
        if (certificateChain == null || certificateChain.isEmpty()) {
            throw new IllegalStateException("The OpenID Connect signing key carries no certificate chain, "
                + "which the %s client identifier prefix requires".formatted(ClientIdentifierPrefixes.X509_SAN_DNS.getValue()));
        }
        val subjectAlternativeNames = certificateChain.getFirst().getSubjectAlternativeNames();
        val matched = subjectAlternativeNames != null && subjectAlternativeNames
            .stream()
            .filter(name -> name.size() == 2 && Integer.valueOf(SUBJECT_ALTERNATIVE_NAME_DNS).equals(name.getFirst()))
            .anyMatch(name -> dnsName.equalsIgnoreCase(Objects.toString(name.getLast(), null)));
        if (!matched) {
            throw new IllegalStateException(("The leaf certificate of the OpenID Connect signing key has no "
                + "dNSName subject alternative name matching [%s]").formatted(dnsName));
        }
        return certificateChain;
    }

    /**
     * Build the wallet deep link. Values are encoded directly rather than through a URI
     * template, so that the braces in the JSON-valued parameters survive as data.
     *
     * @param parameters the query parameters
     * @return the deep link
     */
    protected static String buildDeepLink(final Map<String, Object> parameters) {
        val query = parameters
            .entrySet()
            .stream()
            .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                + '=' + URLEncoder.encode(toParameterValue(entry.getValue()), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
        return "openid4vp://authorize?" + query;
    }

    private static String toParameterValue(final @Nullable Object value) {
        return value instanceof final String text ? text : MAPPER.writeValueAsString(value);
    }

    /**
     * Handle errors.
     *
     * @param ex the ex
     * @return the response entity
     */
    @ExceptionHandler(Exception.class)
    @SuppressWarnings("UnusedMethod")
    private static ResponseEntity<String> handleErrors(final Exception ex) {
        LoggingUtils.error(LOGGER, ex);
        return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(ex.getMessage());
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    public static class OidcVerifiableCredentialPresentationRequest implements Serializable {
        @Serial
        private static final long serialVersionUID = -2051574044900203583L;

        /**
         * Credentials requested from the wallet.
         */
        @NotEmpty
        @Valid
        @JsonProperty("credentials")
        private List<CredentialRequest> credentials;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Getter
        @Setter
        @NoArgsConstructor
        public static class CredentialRequest implements Serializable {
            @Serial
            private static final long serialVersionUID = -4311488519787067866L;

            /**
             * Logical identifier for this request.
             */
            @NotBlank
            private String id;

            /**
             * Credential format, e.g. dc+sd-jwt, jwt_vc_json-ld.
             */
            @NotBlank
            private String format;

            /**
             * Acceptable credential types (VCTs) for dc+sd-jwt.
             */
            @JsonProperty("vct_values")
            private List<String> vctValues;

            /**
             * Requested claims.
             */
            private List<ClaimRequest> claims;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Getter
        @Setter
        @NoArgsConstructor
        public static class ClaimRequest implements Serializable {
            @Serial
            private static final long serialVersionUID = 1203934733451522730L;

            /**
             * Claim path, e.g. ["given_name"] or ["address","street"].
             */
            @NotEmpty
            private List<String> path;

            /**
             * Whether disclosure of this claim is required.
             */
            private boolean required = true;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class OidcVerifiableCredentialPresentationResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = -2135647280123456789L;

        /**
         * Unique request identifier.
         */
        @JsonProperty("request_id")
        private String requestId;

        /**
         * URI where the wallet can fetch the complete authorization request.
         */
        @JsonProperty("request_uri")
        private String requestUri;

        /**
         * Deep link suitable for QR codes or mobile wallets.
         */
        @JsonProperty("authorization_request")
        private String authorizationRequest;

        /**
         * Lifetime of the request in seconds.
         */
        @JsonProperty("expires_in")
        private long expiresIn;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class OidcVerifiableCredentialPresentationClientMetadata implements Serializable {
        @Serial
        private static final long serialVersionUID = 514084664620629089L;

        /**
         * Supported VP formats.
         */
        @JsonProperty("vp_formats_supported")
        @Builder.Default
        private Map<String, VpFormat> vpFormatsSupported = new LinkedHashMap<>();

        /**
         * Human-readable verifier information.
         */
        @JsonProperty("client_name")
        private String clientName;

        @JsonProperty("logo_uri")
        private URI logoUri;

        @JsonProperty("policy_uri")
        private URI policyUri;

        @JsonProperty("tos_uri")
        private URI termsOfServiceUri;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Getter
        @Setter
        @NoArgsConstructor
        @SuperBuilder
        @Jacksonized
        public static class VpFormat implements Serializable {
            @Serial
            private static final long serialVersionUID = 1459533499391098688L;

            /**
             * JWT algorithms supported for JWT-based credentials.
             */
            @JsonProperty("alg_values")
            private List<String> algValues;

            /**
             * Supported issuer SD-JWT algorithms.
             */
            @JsonProperty("sd-jwt_alg_values")
            private List<String> sdJwtAlgValues;

            /**
             * Supported Key Binding JWT algorithms.
             */
            @JsonProperty("kb-jwt_alg_values")
            private List<String> kbJwtAlgValues;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class OidcVerifiableCredentialDCQL implements Serializable {
        @Serial
        private static final long serialVersionUID = 4548552217887285520L;
        /**
         * Logical identifier for this request.
         */
        @NotBlank
        private String id;

        /**
         * Credential format, e.g. dc+sd-jwt, jwt_vc_json-ld.
         */
        @NotBlank
        private String format;

        private Meta meta;

        /**
         * Requested claims.
         */
        private List<DCQLCredentialClaimRequest> claims;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Getter
        @Setter
        @NoArgsConstructor
        @SuperBuilder
        @Jacksonized
        public static class Meta implements Serializable {
            @Serial
            private static final long serialVersionUID = 3646996453536891101L;
            /**
             * Acceptable credential types (VCTs) for dc+sd-jwt.
             */
            @JsonProperty("vct_values")
            private List<String> vctValues;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Getter
        @Setter
        @NoArgsConstructor
        @SuperBuilder
        @Jacksonized
        public static class DCQLCredentialClaimRequest implements Serializable {
            @Serial
            private static final long serialVersionUID = 1103934733451522730L;

            /**
             * Claim path, e.g. ["given_name"] or ["address","street"].
             */
            @NotEmpty
            private List<String> path;
        }
    }
}
