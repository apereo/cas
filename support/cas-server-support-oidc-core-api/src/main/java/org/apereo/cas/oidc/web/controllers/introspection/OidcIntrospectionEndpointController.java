package org.apereo.cas.oidc.web.controllers.introspection;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20IntrospectionEndpointController;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.pac4j.core.context.WebContext;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This is {@link OidcIntrospectionEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcIntrospectionEndpointController extends OAuth20IntrospectionEndpointController<OidcConfigurationContext> {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    public OidcIntrospectionEndpointController(final OidcConfigurationContext context) {
        super(context);
    }

    @GetMapping(consumes = {
        OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MediaType.APPLICATION_JSON_VALUE
    }, produces = {
        OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE,
        MediaType.APPLICATION_JSON_VALUE
    },
        value = {
            '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL,
            "/**/" + OidcConstants.INTROSPECTION_URL
        })
    @Operation(summary = "Handle OIDC introspection request")
    @Override
    public ResponseEntity handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, OidcConstants.INTROSPECTION_URL)) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return new ResponseEntity(body, HttpStatus.BAD_REQUEST);
        }
        return super.handleRequest(request, response);
    }

    @PostMapping(consumes = {
        OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE,
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE
    },
        value = {
            '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL,
            "/**/" + OidcConstants.INTROSPECTION_URL
        })
    @Operation(summary = "Handle OIDC introspection request")
    @Override
    public ResponseEntity handlePostRequest(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        return super.handlePostRequest(request, response);
    }
    
    @Override
    protected ResponseEntity buildIntrospectionEntityResponse(final WebContext context,
                                                              final OAuth20IntrospectionAccessTokenResponse introspect) {
        val responseEntity = super.buildIntrospectionEntityResponse(context, introspect);
        return context.getRequestHeader(HttpHeaders.ACCEPT)
            .filter(headerValue -> StringUtils.equalsAnyIgnoreCase(headerValue, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE))
            .map(headerValue -> {
                val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                    getConfigurationContext().getServicesManager(), introspect.getClientId());
                val signingAndEncryptionService = getConfigurationContext().getIntrospectionSigningAndEncryptionService();
                return FunctionUtils.doAndHandle(() -> {
                    if (signingAndEncryptionService.shouldSignToken(registeredService) || signingAndEncryptionService.shouldEncryptToken(registeredService)) {
                        return signAndEncryptIntrospection(context, introspect, registeredService);
                    }
                    return buildPlainIntrospectionClaims(context, introspect, registeredService);
                }, e -> ResponseEntity.badRequest().body("Unable to produce introspection JWT claims")).get();
            })
            .orElse(responseEntity);
    }

    protected ResponseEntity<String> buildPlainIntrospectionClaims(final WebContext context,
                                                                   final OAuth20IntrospectionAccessTokenResponse introspect,
                                                                   final OAuthRegisteredService registeredService) throws Exception {
        val claims = convertIntrospectionIntoClaims(introspect, registeredService);
        val jwt = new PlainJWT(JWTClaimsSet.parse(claims.getClaimsMap()));
        val jwtRequest = jwt.serialize();
        return buildResponseEntity(jwtRequest, registeredService);
    }

    private JwtClaims convertIntrospectionIntoClaims(final OAuth20IntrospectionAccessTokenResponse introspect,
                                                     final OAuthRegisteredService registeredService) throws Exception {
        val signingAndEncryptionService = getConfigurationContext().getIntrospectionSigningAndEncryptionService();
        val claims = new JwtClaims();
        claims.setIssuer(signingAndEncryptionService.resolveIssuer(Optional.of(registeredService)));
        claims.setAudience(registeredService.getClientId());
        claims.setIssuedAt(NumericDate.now());
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setClaim("token_introspection", MAPPER.readValue(MAPPER.writeValueAsString(introspect), Map.class));
        return claims;
    }

    protected ResponseEntity<String> signAndEncryptIntrospection(final WebContext context,
                                                                 final OAuth20IntrospectionAccessTokenResponse introspect,
                                                                 final OAuthRegisteredService registeredService) throws Throwable {
        val claims = convertIntrospectionIntoClaims(introspect, registeredService);
        LOGGER.debug("Collected introspection claims, before cipher operations, are [{}]", claims);
        val signingAndEncryptionService = getConfigurationContext().getIntrospectionSigningAndEncryptionService();
        val result = signingAndEncryptionService.encode(registeredService, claims);
        LOGGER.debug("Finalized introspection JWT is [{}]", result);
        return buildResponseEntity(result, registeredService);
    }

    private static ResponseEntity<String> buildResponseEntity(final String result,
                                                              final OAuthRegisteredService registeredService) {
        val context = CollectionUtils.<String, Object>wrap(
            HttpHeaders.CONTENT_TYPE, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE,
            "Client ID", registeredService.getClientId(),
            "Service", registeredService.getName());
        LoggingUtils.protocolMessage("OpenID Connect Introspection Response", context, result);
        val headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, CollectionUtils.wrapList(OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE));
        return ResponseEntity.ok().headers(headers).body(result);
    }
}
