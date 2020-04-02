package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20RevocationEndpointController;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcRevocationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OidcRevocationEndpointController extends OAuth20RevocationEndpointController {

    public OidcRevocationEndpointController(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.REVOCATION_URL,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ModelAndView handleRequest(final HttpServletRequest request,
                                      final HttpServletResponse response) {
        return super.handleRequest(request, response);
    }
}
