package org.apereo.cas.oidc.vc.issuer.web;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.util.LoggingUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcVerifiableCredentialNonceEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialNonceEndpointController extends BaseOAuth20Controller<OidcConfigurationContext> {

    private final OidcVerifiableCredentialNonceService credentialNonceService;

    public OidcVerifiableCredentialNonceEndpointController(
        final OidcConfigurationContext configurationContext,
        final OidcVerifiableCredentialNonceService credentialNonceService) {
        super(configurationContext);
        this.credentialNonceService = credentialNonceService;
    }

    /**
     * Handle response entity.
     *
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_NONCE_URL,
        "/**/" + OidcConstants.VC_NONCE_URL
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handle(
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) {

        val webContext = new JEEContext(httpRequest, httpResponse);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.VC_NONCE_URL))) {
            LOGGER.warn("CAS cannot accept the request given the issuer is invalid.");
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return ResponseEntity.badRequest().body(body);
        }
        val nonce = credentialNonceService.create();
        return ResponseEntity.ok().body(
            Map.of("c_nonce", nonce.value(), "expires_at", nonce.expiresAt()));
    }

    /**
     * Handle errors.
     *
     * @param ex the ex
     * @return the response entity
     */
    @ExceptionHandler(Exception.class)
    @SuppressWarnings("UnusedMethod")
    private static ResponseEntity<String> handle(final Exception ex) {
        LoggingUtils.error(LOGGER, ex);
        if (ex instanceof final ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(rse.getReason());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
