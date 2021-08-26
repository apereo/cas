package org.apereo.cas.oidc.web.controllers.profile;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointController;

import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcUserProfileEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcUserProfileEndpointController extends OAuth20UserProfileEndpointController<OidcConfigurationContext> {

    public OidcUserProfileEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.PROFILE_URL,
        "/**/" + OidcConstants.PROFILE_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<String> handleGetRequest(final HttpServletRequest request,
                                                   final HttpServletResponse response) throws Exception {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getOidcRequestSupport().isValidIssuerForEndpoint(webContext, OidcConstants.PROFILE_URL)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return super.handleGetRequest(request, response);
    }

    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.PROFILE_URL,
        "/**/" + OidcConstants.PROFILE_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<String> handlePostRequest(final HttpServletRequest request,
                                                    final HttpServletResponse response) throws Exception {
        return handleGetRequest(request, response);
    }
}
