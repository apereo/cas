package org.apereo.cas.oidc.vc.offer.web;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialOfferService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.util.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcVerifiableCredentialOfferEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialOfferEndpointController extends BaseOAuth20Controller<OidcConfigurationContext> {
    private final OidcVerifiableCredentialOfferService credentialOfferService;

    public OidcVerifiableCredentialOfferEndpointController(
        final OidcConfigurationContext configurationContext,
        final OidcVerifiableCredentialOfferService credentialOfferService) {
        super(configurationContext);
        this.credentialOfferService = credentialOfferService;
    }

    /**
     * Handle and procuce response entity.
     *
     * @param transactionId the transaction id
     * @param httpRequest   the http request
     * @param httpResponse  the http response
     * @return the response entity
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_URL + "/{transactionId}",
        "/**/" + OidcConstants.VC_CREDENTIAL_OFFER_URL + "/{transactionId}"},
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC credential offer request",
        description = "Handles requests for OIDC credential offer issuance",
        parameters = @Parameter(name = "transactionId", in = ParameterIn.PATH, description = "Transaction ID"))
    public ResponseEntity handle(
        @PathVariable final String transactionId,
        final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) {
        return ResponseEntity.ok(credentialOfferService.fetch(transactionId));
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handle and create response entity.
     *
     * @param request      the request
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_TRANSACTIONS_URL,
        "/**/" + OidcConstants.VC_CREDENTIAL_OFFER_TRANSACTIONS_URL
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC credential offer request",
        description = "Handles requests for OIDC credential offer issuance")
    public ResponseEntity handle(
        @RequestBody final OidcVerifiableCredentialTransactionRequest request,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) {
        val context = new JEEContext(httpRequest, httpResponse);
        val profile = OAuth20Utils.getAuthenticatedUserProfile(context, getConfigurationContext().getSessionStore());
        LOGGER.debug("Checking credential configuration IDs for [{}]", profile.getId());

        val credentialConfigurationIds = configurationContext.getCasProperties().getAuthn().getOidc().getVc()
            .getIssuer().getCredentialConfigurations().keySet();
        if (credentialConfigurationIds.isEmpty() || !credentialConfigurationIds.containsAll(request.credentialConfigurationIds())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Unauthorized credential configuration id requested"));
        }
        val clientId = profile.getAttribute(OAuth20Constants.CLIENT_ID).toString();
        val offer = credentialOfferService.create(clientId, request.principal(), request.credentialConfigurationIds());
        val txCode = offer.getGrants().getPreAuthorizedCodeGrant().getTxCode();
        val offerUri = getConfigurationContext().getCasProperties().getServer().getPrefix()
            + '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_URL
            + '/' + txCode;
        return ResponseEntity.ok(
            Map.of(
                "transactionId", txCode,
                "credentialOfferUri", offerUri
            )
        );
    }

    public record OidcVerifiableCredentialTransactionRequest(
        String principal,
        List<String> credentialConfigurationIds) {
    }
}
