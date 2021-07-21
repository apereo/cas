package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.val;
import org.pac4j.core.context.JEEContext;
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
public class OidcClientConfigurationEndpointController extends BaseOidcController {
    public OidcClientConfigurationEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    /**
     * Handle request response entity.
     *
     * @param clientId the client id
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CLIENT_CONFIGURATION_URL,
        "/**/" + OidcConstants.CLIENT_CONFIGURATION_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handleRequestInternal(
        @RequestParam(name = OidcConstants.CLIENT_REGISTRATION_CLIENT_ID) final String clientId,
        final HttpServletRequest request, final HttpServletResponse response) {

        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getOidcRequestSupport().isValidIssuerForEndpoint(webContext, OidcConstants.CLIENT_CONFIGURATION_URL)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId);
        if (service instanceof OidcRegisteredService) {
            val prefix = getConfigurationContext().getCasProperties().getServer().getPrefix();
            val regResponse = OidcClientRegistrationUtils.getClientRegistrationResponse((OidcRegisteredService) service, prefix);
            return new ResponseEntity<>(regResponse, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
