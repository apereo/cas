package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20RevocationEndpointController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcRevocationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag(name = "OpenID Connect")
public class OidcRevocationEndpointController extends OAuth20RevocationEndpointController<OidcConfigurationContext> {

    public OidcRevocationEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.REVOCATION_URL,
        "/**/" + OidcConstants.REVOCATION_URL
    },
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    @Operation(summary = "Handle OIDC token revocation request")
    public ModelAndView handleRequest(final HttpServletRequest request,
                                      final HttpServletResponse response) throws Throwable {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, OidcConstants.REVOCATION_URL)) {
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
        }
        return super.handleRequest(request, response);
    }
}
