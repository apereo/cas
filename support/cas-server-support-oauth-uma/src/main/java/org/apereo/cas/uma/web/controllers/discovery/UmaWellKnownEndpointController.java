package org.apereo.cas.uma.web.controllers.discovery;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.discovery.UmaServerDiscoverySettings;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This is {@link UmaWellKnownEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Controller("umaWellKnownEndpointController")
@RequiredArgsConstructor
public class UmaWellKnownEndpointController {
    private final UmaServerDiscoverySettings discovery;

    /**
     * Gets well known uma discovery configuration.
     *
     * @return the well known discovery configuration
     */
    @GetMapping(value = '/' + OAuth20Constants.BASE_OAUTH20_URL + "/.well-known/uma-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UmaServerDiscoverySettings> getWellKnownDiscoveryConfiguration() {
        return new ResponseEntity(this.discovery, HttpStatus.OK);
    }
}
