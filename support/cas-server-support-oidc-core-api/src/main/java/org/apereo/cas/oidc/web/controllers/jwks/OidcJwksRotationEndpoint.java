package org.apereo.cas.oidc.web.controllers.jwks;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
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
@Endpoint(id = "oidcJwks", defaultAccess = Access.NONE)
public class OidcJwksRotationEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<OidcJsonWebKeystoreRotationService> rotationService;

    public OidcJwksRotationEndpoint(final CasConfigurationProperties casProperties,
                                    final ConfigurableApplicationContext applicationContext,
                                    final ObjectProvider<OidcJsonWebKeystoreRotationService> rotationService) {
        super(casProperties, applicationContext);
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
