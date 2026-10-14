package org.apereo.cas.oidc.vc.issuer.web;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20DPoPNonceService;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.util.LoggingUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.CacheControl;
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

    private final OAuth20DPoPNonceService dpopNonceService;

    public OidcVerifiableCredentialNonceEndpointController(
        final OidcConfigurationContext configurationContext,
        final OidcVerifiableCredentialNonceService credentialNonceService,
        final OAuth20DPoPNonceService dpopNonceService) {
        super(configurationContext);
        this.credentialNonceService = credentialNonceService;
        this.dpopNonceService = dpopNonceService;
    }

    /**
     * Hand out a {@code c_nonce} and, once DPoP nonces are turned on, a DPoP nonce in the {@code DPoP-Nonce} header for the
     * DPoP proof of the credential request (OpenID4VCI 1.0, section 7.2).
     *
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     * @throws Throwable the throwable
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_NONCE_URL,
        "/**/" + OidcConstants.VC_NONCE_URL
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handle(
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse)
            throws Throwable {

        val webContext = new JEEContext(httpRequest, httpResponse);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.VC_NONCE_URL))) {
            LOGGER.warn("CAS cannot accept the request given the issuer is invalid.");
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return ResponseEntity.badRequest().body(body);
        }
        val nonce = credentialNonceService.create();
        if (dpopNonceService.isEnabled()) {
            dpopNonceService.provide(webContext);
        }
        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noStore())
            .body(Map.of(OidcConstants.C_NONCE, nonce.value()));
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
