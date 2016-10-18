package org.apereo.cas.web.controllers;

import org.apereo.cas.OidcConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.web.OAuth20ProfileController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OidcProfileEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcProfileEndpointController extends OAuth20ProfileController {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RequestMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuthConstants.PROFILE_URL,
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    protected ResponseEntity<String> handleRequestInternal(final HttpServletRequest request,
                                                           final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }

    @Override
    protected Map<String, Object> writeOutProfileResponse(final Authentication authentication,
                                                          final Principal principal) throws IOException {
        final Map<String, Object> map = new HashMap<>(principal.getAttributes());
        if (!map.containsKey(OidcConstants.CLAIM_SUB)) {
            map.put(OidcConstants.CLAIM_SUB, principal.getId());
        }
        map.put(OidcConstants.CLAIM_AUTH_TIME, authentication.getAuthenticationDate().toEpochSecond());
        return map;
    }
}
