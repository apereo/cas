package org.apereo.cas.oidc.web.controllers.introspection;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20IntrospectionEndpointController;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link OidcIntrospectionEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OidcIntrospectionEndpointController extends OAuth20IntrospectionEndpointController<OidcConfigurationContext> {
    public OidcIntrospectionEndpointController(final OidcConfigurationContext context) {
        super(context);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(consumes = {
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MediaType.APPLICATION_JSON_VALUE
    }, produces = MediaType.APPLICATION_JSON_VALUE,
        value = {
            '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL,
            "/**/" + OidcConstants.INTROSPECTION_URL
        })
    @Override
    public ResponseEntity<OAuth20IntrospectionAccessTokenResponse> handleRequest(
        final HttpServletRequest request,
        final HttpServletResponse response) {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, OidcConstants.INTROSPECTION_URL)) {
            val body = OAuth20Utils.toJson(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer"));
            return new ResponseEntity(body, HttpStatus.BAD_REQUEST);
        }
        return super.handleRequest(request, response);
    }

    /**
     * Handle post request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @PostMapping(consumes = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE
    }, produces = MediaType.APPLICATION_JSON_VALUE,
        value = {
            '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL,
            "/**/" + OidcConstants.INTROSPECTION_URL
        })
    @Override
    public ResponseEntity<OAuth20IntrospectionAccessTokenResponse> handlePostRequest(final HttpServletRequest request,
                                                                                     final HttpServletResponse response) {
        return super.handlePostRequest(request, response);
    }

    @Override
    protected OAuth20IntrospectionAccessTokenResponse createIntrospectionValidResponse(final OAuth20Token ticket) {
        val r = super.createIntrospectionValidResponse(ticket);
        r.setIss(getConfigurationContext().getIssuerService().determineIssuer(Optional.empty()));
        FunctionUtils.doIf(r.isActive(), o -> r.setScope(String.join(" ", ticket.getScopes()))).accept(r);
        return r;
    }
}
