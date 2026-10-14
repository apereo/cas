package org.apereo.cas.oidc.web.controllers.authorize;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.LoggingUtils;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPNonceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcPushedAuthorizeEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcPushedAuthorizeEndpointController extends OidcAuthorizeEndpointController {
    public OidcPushedAuthorizeEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    @GetMapping("/**/" + OidcConstants.PUSHED_AUTHORIZE_URL)
    @Operation(summary = "Handle OIDC Pushed authorization request")
    public ModelAndView handleRequest(final HttpServletRequest request,
                                      final HttpServletResponse response) {
        return OAuth20Utils.produceUnauthorizedErrorView(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    @PostMapping("/**/" + OidcConstants.PUSHED_AUTHORIZE_URL)
    @Operation(summary = "Handle OIDC Pushed Authorization Request")
    public ModelAndView handleRequestPost(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.PUSHED_AUTHORIZE_URL))) {
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
        }
        val keyBindingError = verifyProofOfPossessionKey(request, response, webContext);
        return keyBindingError != null ? keyBindingError : super.handleRequest(request, response);
    }

    /**
     * A DPoP proof sent with the pushed authorization request is verified as it would be at the token endpoint, nonce included,
     * and its key thumbprint stands for {@code dpop_jkt}, binding the authorization code to that key; a {@code dpop_jkt} in the
     * request must then name the same key (RFC 9449, section 10.1).
     *
     * @param request    the request
     * @param response   the response
     * @param webContext the web context
     * @return the error, or null when the request carries no DPoP proof or a valid one
     */
    protected @Nullable ModelAndView verifyProofOfPossessionKey(final HttpServletRequest request, final HttpServletResponse response,
                                                                final JEEContext webContext) {
        try {
            val thumbprint = getConfigurationContext().getProofOfPossessionValidator().validateKeyBinding(webContext);
            if (thumbprint.isPresent()) {
                val requestedThumbprint = getConfigurationContext().getRequestParameterResolver()
                    .resolveRequestParameter(webContext, OAuth20Constants.DPOP_JKT)
                    .filter(StringUtils::isNotBlank);
                if (requestedThumbprint.isPresent() && !requestedThumbprint.get().equals(thumbprint.get())) {
                    return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_DPOP_PROOF,
                        "dpop_jkt does not match the key of the DPoP proof");
                }
                webContext.setRequestAttribute(OAuth20Constants.DPOP_JKT, thumbprint.get());
            }
            return null;
        } catch (final InvalidDPoPNonceException e) {
            return OAuth20Utils.writeError(response, OAuth20Constants.USE_DPOP_NONCE, e.getMessage());
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_DPOP_PROOF, "DPoP proof validation failed");
        }
    }
}
