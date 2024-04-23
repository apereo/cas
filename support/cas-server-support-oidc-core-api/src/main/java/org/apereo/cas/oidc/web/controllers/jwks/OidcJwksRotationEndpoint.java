package org.apereo.cas.oidc.web.controllers.jwks;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This is {@link OidcJwksRotationEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RestControllerEndpoint(id = "oidcJwks", enableByDefault = false)
public class OidcJwksRotationEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<OidcJsonWebKeystoreRotationService> rotationService;

    public OidcJwksRotationEndpoint(final CasConfigurationProperties casProperties,
                                    final ObjectProvider<OidcJsonWebKeystoreRotationService> rotationService) {
        super(casProperties);
        this.rotationService = rotationService;
    }

    /**
     * Rotate keys and response entity.
     *
     * @return the response entity
     * @throws Throwable the exception
     */
    @GetMapping(path = "/rotate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rotate keys in the keystore forcefully")
    public ResponseEntity<String> handleRotation() throws Throwable {
        val rotation = rotationService.getObject().rotate();
        return new ResponseEntity<>(
            rotation.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY), HttpStatus.OK);
    }

    /**
     * Revoke keys and response entity.
     *
     * @return the response entity
     * @throws Throwable the exception
     */
    @GetMapping(path = "/revoke", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Revoke keys in the keystore forcefully")
    public ResponseEntity<String> handleRevocation() throws Throwable {
        val rotation = rotationService.getObject().revoke();
        return new ResponseEntity<>(
            rotation.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY), HttpStatus.OK);
    }

}
