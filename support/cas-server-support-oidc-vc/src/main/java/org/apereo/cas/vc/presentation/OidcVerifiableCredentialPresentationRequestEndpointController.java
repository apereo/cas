package org.apereo.cas.vc.presentation;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * This is {@link OidcVerifiableCredentialPresentationRequestEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialPresentationRequestEndpointController extends BaseOAuth20Controller<OidcConfigurationContext> {
    public OidcVerifiableCredentialPresentationRequestEndpointController(
        final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    /**
     * Fetch request response entity.
     *
     * @param requestId    the request id
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL + "/{requestId}",
        "/**/" + OidcConstants.VC_PRESENTATION_REQUEST_URL + "/{requestId}"
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC verifiable credential presentation request",
        description = "Handles requests for OIDC verifiable credential presentation issuance")
    public ResponseEntity fetchRequest(
        @PathVariable final String requestId,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) {

        val transientSessionTicket = configurationContext.getTicketRegistry().getTicket(requestId, TransientSessionTicket.class);
        Objects.requireNonNull(transientSessionTicket, () -> "No request could be found for the given request id: " + requestId);
        val issuer = configurationContext.getCasProperties().getAuthn().getOidc().getCore().getIssuer();
        val clientId = "redirect_uri:" + issuer + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;

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

        return ResponseEntity.ok(
            Map.of(
                "response_uri", issuer + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL,
                "client_id", clientId,
                "response_type", "vp_token",
                "response_mode", "direct_post",
                "nonce", Objects.requireNonNull(transientSessionTicket.getPropertyAsString("nonce")),
                "state", Objects.requireNonNull(transientSessionTicket.getPropertyAsString("state")),
                "dcql_query", Map.of("credentials", dcqlCredentials),
                "client_metadata", clientMetadata
            )
        );
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

        val issuer = configurationContext.getCasProperties().getAuthn().getOidc().getCore().getIssuer();

        val factory = (TransientSessionTicketFactory) configurationContext.getTicketFactory().get(TransientSessionTicket.class);
        val transientSessionTicket = factory.create(CollectionUtils.wrap(
            "nonce", UUID.randomUUID().toString(),
            "credentials", request.getCredentials()
        ));
        transientSessionTicket.putProperty("state", transientSessionTicket.getId());
        val addedTicket = configurationContext.getTicketRegistry().addTicket(transientSessionTicket);

        val requestUri = issuer + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL + '/' + addedTicket.getId();
        val clientId = "redirect_uri:" + issuer + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;

        val authorizationRequest = UriComponentsBuilder
            .fromUriString("openid4vp://authorize")
            .queryParam(OAuth20Constants.CLIENT_ID, clientId)
            .queryParam(OidcConstants.REQUEST_URI, requestUri)
            .build()
            .encode()
            .toUriString();

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
