package org.apereo.cas.oidc.web.controllers.authorize;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This is {@link OidcPushedAuthorizeEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag(name = "OpenID Connect")
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
    @Operation(summary = "Handle OIDC Pushed authorization request")
    public ModelAndView handleRequestPost(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.PUSHED_AUTHORIZE_URL))) {
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
        }
        return super.handleRequest(request, response);
    }
}
