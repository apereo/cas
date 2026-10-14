package org.apereo.cas.oidc.vc.issuer.web;

import module java.base;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialSigningUtils;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadata;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadataService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcVerifiableCredentialIssuerMetadataController}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialIssuerMetadataController extends BaseOAuth20Controller<OidcConfigurationContext> {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false)
        .minimal(true)
        .build()
        .toObjectMapper();

    private static final MediaType SIGNED_METADATA = MediaType.parseMediaType(OidcConstants.CONTENT_TYPE_JWT);

    private final OidcCredentialIssuerMetadataService metadataService;

    public OidcVerifiableCredentialIssuerMetadataController(final OidcConfigurationContext configurationContext,
                                                            final OidcCredentialIssuerMetadataService metadataService) {
        super(configurationContext);
        this.metadataService = metadataService;
    }

    /**
     * Credential issuer metadata, as JSON or, for a wallet that asks for {@code application/jwt}, as signed metadata
     * (OpenID4VCI 1.0 section 12.2.3). Both vary by {@code Accept}, so caches keep them apart, and errors are always JSON.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     * @throws Throwable the throwable
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER_URL,
        "/**/" + OidcConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER_URL
    }, produces = {MediaType.APPLICATION_JSON_VALUE, OidcConstants.CONTENT_TYPE_JWT})
    @Operation(summary = "Handle OIDC credential issuer metadata request",
        description = "Handles requests for well-known OIDC credential issuer metadata")
    public ResponseEntity handle(final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws Throwable {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER_URL))) {
            LOGGER.warn("CAS cannot accept the request given the issuer is invalid.");
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(body);
        }
        val body = metadataService.build();
        if (isSignedMetadataRequested(request)) {
            return ResponseEntity.ok().varyBy(HttpHeaders.ACCEPT).contentType(SIGNED_METADATA).body(signMetadata(body));
        }
        return ResponseEntity.ok().varyBy(HttpHeaders.ACCEPT).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    /**
     * Signed credential issuer metadata: every metadata parameter as a top-level claim, with {@code sub} and {@code iss}
     * naming the credential issuer, {@code iat} and {@code exp}, typed {@code openidvci-issuer-metadata+jwt} and signed with
     * the issuer signing key and its {@code x5c} chain, if any.
     *
     * @param metadata the metadata
     * @return the signed metadata
     * @throws Throwable the throwable
     */
    protected String signMetadata(final OidcCredentialIssuerMetadata metadata) throws Throwable {
        val claims = JwtClaims.parse(MAPPER.writeValueAsString(metadata));
        claims.setSubject(metadata.getCredentialIssuer());
        claims.setIssuer(metadata.getCredentialIssuer());
        val issuedAt = NumericDate.now();
        claims.setIssuedAt(issuedAt);
        val expiration = Beans.newDuration(getConfigurationContext().getCasProperties().getAuthn().getOidc()
            .getVc().getMetadata().getSignedMetadataExpiration());
        claims.setExpirationTime(NumericDate.fromSeconds(issuedAt.getValue() + expiration.toSeconds()));
        return OidcVerifiableCredentialSigningUtils.sign(getConfigurationContext(), claims, "openidvci-issuer-metadata+jwt");
    }

    /**
     * Whether the wallet prefers signed metadata: its {@code Accept} header names {@code application/jwt} at least as
     * favorably as anything JSON could satisfy. A wildcard alone is answered with JSON.
     *
     * @param request the request
     * @return true when signed metadata is requested
     */
    protected static boolean isSignedMetadataRequested(final HttpServletRequest request) {
        val accept = request.getHeader(HttpHeaders.ACCEPT);
        if (StringUtils.isBlank(accept)) {
            return false;
        }
        val mediaTypes = MediaType.parseMediaTypes(accept);
        val signedQuality = mediaTypes.stream().filter(SIGNED_METADATA::equalsTypeAndSubtype)
            .mapToDouble(MediaType::getQualityValue).max().orElse(0);
        val jsonQuality = mediaTypes.stream().filter(type -> !SIGNED_METADATA.equalsTypeAndSubtype(type)
                && type.isCompatibleWith(MediaType.APPLICATION_JSON))
            .mapToDouble(MediaType::getQualityValue).max().orElse(0);
        return signedQuality > 0 && signedQuality >= jsonQuality;
    }

    /**
     * JWT VC Issuer Metadata, as defined by SD-JWT VC: where a verifier other than CAS finds the keys that sign
     * the credentials CAS issues. Its {@code issuer} equals the {@code iss} of every credential, and its
     * {@code jwks_uri} is the OpenID Connect JWKS, which publishes the credential signing keys under the {@code kid}
     * each credential names. A verifier locates this document by inserting {@code /.well-known/jwt-vc-issuer}
     * between the host and the path of {@code iss}, so a deployment under a context path routes it the same way as
     * the credential issuer metadata.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_JWT_VC_ISSUER_URL,
        "/**/" + OidcConstants.WELL_KNOWN_JWT_VC_ISSUER_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle JWT VC issuer metadata request",
        description = "Publishes where verifiers find the keys that sign issued verifiable credentials")
    public ResponseEntity handleJwtVcIssuerMetadata(final HttpServletRequest request,
                                                    final HttpServletResponse response) {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.WELL_KNOWN_JWT_VC_ISSUER_URL))) {
            LOGGER.warn("CAS cannot accept the request given the issuer is invalid.");
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return ResponseEntity.badRequest().body(body);
        }
        val body = new LinkedHashMap<String, Object>();
        body.put("issuer", getConfigurationContext().getCasProperties().getAuthn().getOidc().getCore().getIssuer());
        body.put("jwks_uri", getConfigurationContext().getDiscoverySettings().getJwksUri());
        return ResponseEntity.ok().body(body);
    }
}
