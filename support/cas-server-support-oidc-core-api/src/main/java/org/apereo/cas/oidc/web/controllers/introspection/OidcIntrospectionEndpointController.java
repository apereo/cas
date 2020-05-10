package org.apereo.cas.oidc.web.controllers.introspection;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20IntrospectionEndpointController;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcIntrospectionEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OidcIntrospectionEndpointController extends OAuth20IntrospectionEndpointController {
    public OidcIntrospectionEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE,
        value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL)
    @Override
    public ResponseEntity<OAuth20IntrospectionAccessTokenResponse> handleRequest(final HttpServletRequest request,
                                                                                 final HttpServletResponse response) {
        return super.handleRequest(request, response);
    }

    /**
     * Handle post request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE,
        value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL)
    @Override
    public ResponseEntity<OAuth20IntrospectionAccessTokenResponse> handlePostRequest(final HttpServletRequest request,
                                                                                     final HttpServletResponse response) {
        return super.handlePostRequest(request, response);
    }

    @Override
    protected OAuth20IntrospectionAccessTokenResponse createIntrospectionValidResponse(final OAuthRegisteredService service, final OAuth20AccessToken ticket) {
        val r = super.createIntrospectionValidResponse(service, ticket);
        if (r.isActive()) {
            r.setScope(String.join(" ", ticket.getScopes()));
        }
        return r;
    }
}
