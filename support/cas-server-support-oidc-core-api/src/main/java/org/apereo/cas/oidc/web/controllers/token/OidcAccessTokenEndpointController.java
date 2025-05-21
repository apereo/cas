package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPProofException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcAccessTokenEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag(name = "OpenID Connect")
public class OidcAccessTokenEndpointController extends OAuth20AccessTokenEndpointController<OidcConfigurationContext> {

    public OidcAccessTokenEndpointController(final OidcConfigurationContext oauthConfigurationContext,
                                             final AuditableExecution accessTokenGrantAuditableRequestExtractor) {
        super(oauthConfigurationContext, accessTokenGrantAuditableRequestExtractor);
    }

    @PostMapping({
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL,
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.TOKEN_URL,
        "/**/" + OidcConstants.ACCESS_TOKEN_URL,
        "/**/" + OidcConstants.TOKEN_URL
    })
    @Operation(summary = "Handle OIDC access token request")
    @Override
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val webContext = new JEEContext(request, response);
        val issuerService = getConfigurationContext().getIssuerService();
        if (!issuerService.validateIssuer(webContext, OidcConstants.ACCESS_TOKEN_URL)
            && !issuerService.validateIssuer(webContext, OidcConstants.TOKEN_URL)) {
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
        }
        return super.handleRequest(request, response);
    }

    @GetMapping({
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL,
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.TOKEN_URL,
        "/**/" + OidcConstants.ACCESS_TOKEN_URL,
        "/**/" + OidcConstants.TOKEN_URL
    })
    @Operation(summary = "Handle OIDC access token request")
    @Override
    public ModelAndView handleGetRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return this.handleRequest(request, response);
    }

    /**
     * Handle invalid DPoP proof exception.
     *
     * @param req the req
     * @param ex  the ex
     * @return the model and view
     */
    @ExceptionHandler(InvalidDPoPProofException.class)
    public ModelAndView handleInvalidDPoPProofException(final HttpServletResponse req, final Exception ex) {
        return OAuth20Utils.writeError(req, OAuth20Constants.INVALID_DPOP_PROOF);
    }
}
