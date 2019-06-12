package org.apereo.cas.oidc.web.controllers.profile;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointController;

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
public class OidcUserProfileEndpointController extends OAuth20UserProfileEndpointController {

    public OidcUserProfileEndpointController(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.PROFILE_URL,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<String> handleGetRequest(final HttpServletRequest request,
                                                   final HttpServletResponse response) throws Exception {
        return super.handleGetRequest(request, response);
    }

    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.PROFILE_URL,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<String> handlePostRequest(final HttpServletRequest request,
                                                    final HttpServletResponse response) throws Exception {
        return super.handlePostRequest(request, response);
    }
}
