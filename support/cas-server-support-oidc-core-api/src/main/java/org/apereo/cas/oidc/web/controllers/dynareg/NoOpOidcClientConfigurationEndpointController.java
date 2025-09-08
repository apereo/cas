package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcNoOpClientConfigurationEndpointController}.
 * Acts as a disabled placeholder when dynamic client registration is turned off.
 *
 * @author Jiří Prokop
 * @since 7.3.0
 */
@Slf4j
public class NoOpOidcClientConfigurationEndpointController extends OidcClientConfigurationEndpointController {
    public NoOpOidcClientConfigurationEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public ResponseEntity<?> handleRequestInternal(
        @RequestParam(name = OAuth20Constants.CLIENT_ID, required = false) final String clientId,
        final HttpServletRequest request, final HttpServletResponse response) {
        LOGGER.debug("Client configuration endpoint disabled: GET request rejected for clientId=[{}]", clientId);
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<?> handleUpdates(
        @RequestParam(name = OAuth20Constants.CLIENT_ID, required = false) final String clientId,
        @RequestBody(required = false) final String jsonInput,
        final HttpServletRequest request, final HttpServletResponse response) {
        LOGGER.debug("Client configuration endpoint disabled: PATCH request rejected for clientId=[{}]", clientId);
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
