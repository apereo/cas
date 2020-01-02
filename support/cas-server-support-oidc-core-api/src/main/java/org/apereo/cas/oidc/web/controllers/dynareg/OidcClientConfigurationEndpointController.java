package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcClientConfigurationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OidcClientConfigurationEndpointController extends BaseOAuth20Controller {
    public OidcClientConfigurationEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request response entity.
     *
     * @param clientId the client id
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CLIENT_CONFIGURATION_URL,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handleRequestInternal(
        @RequestParam(name = OidcConstants.CLIENT_REGISTRATION_CLIENT_ID) final String clientId,
        final HttpServletRequest request, final HttpServletResponse response) {

        val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(getOAuthConfigurationContext().getServicesManager(), clientId);
        if (service instanceof OidcRegisteredService) {
            val prefix = getOAuthConfigurationContext().getCasProperties().getServer().getPrefix();
            val regResponse = OidcClientRegistrationUtils.getClientRegistrationResponse((OidcRegisteredService) service, prefix);
            return new ResponseEntity<>(regResponse, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
