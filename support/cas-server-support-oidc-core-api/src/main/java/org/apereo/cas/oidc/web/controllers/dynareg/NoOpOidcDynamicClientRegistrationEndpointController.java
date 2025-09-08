package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.OidcConfigurationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class NoOpOidcDynamicClientRegistrationEndpointController extends OidcDynamicClientRegistrationEndpointController {
    public NoOpOidcDynamicClientRegistrationEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public ResponseEntity handleRequestInternal(
        final String jsonInput,
        final HttpServletRequest request,
        final HttpServletResponse response
    ) {
        LOGGER.debug("OIDC Dynamic Client Registration endpoint is disabled.");
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
