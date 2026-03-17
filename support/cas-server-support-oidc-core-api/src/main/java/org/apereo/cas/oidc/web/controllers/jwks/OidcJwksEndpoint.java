package org.apereo.cas.oidc.web.controllers.jwks;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationStore;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * This is {@link OidcJwksEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Endpoint(id = "oidcJwks", defaultAccess = Access.NONE)
public class OidcJwksEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<OidcJsonWebKeystoreRotationService> rotationService;
    private final ObjectProvider<ClientJwksRegistrationStore> clientJwksRegistrationStore;

    public OidcJwksEndpoint(final CasConfigurationProperties casProperties,
                            final ConfigurableApplicationContext applicationContext,
                            final ObjectProvider<OidcJsonWebKeystoreRotationService> rotationService,
                            final ObjectProvider<ClientJwksRegistrationStore> clientJwksRegistrationStore) {
        super(casProperties, applicationContext);
        this.rotationService = rotationService;
        this.clientJwksRegistrationStore = clientJwksRegistrationStore;
    }

    /**
     * Rotate keys and response entity.
     *
     * @return the response entity
     */
    @GetMapping(path = "/rotate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rotate keys in the keystore forcefully")
    public ResponseEntity<String> handleRotation() {
        val rotation = rotationService.getObject().rotate();
        return new ResponseEntity<>(
            rotation.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY), HttpStatus.OK);
    }

    /**
     * Revoke keys and response entity.
     *
     * @return the response entity
     */
    @GetMapping(path = "/revoke", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Revoke keys in the keystore forcefully")
    public ResponseEntity<String> handleRevocation() {
        val rotation = rotationService.getObject().revoke();
        return new ResponseEntity<>(
            rotation.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY), HttpStatus.OK);
    }

    /**
     * Load client JWKS entries.
     *
     * @return the response entity
     */
    @GetMapping(path = "/clients", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Load all client JWKS entries",
        description = "This endpoint allows retrieval of all client JSON Web Keys registered")
    public ResponseEntity loadClientJwksEntries() {
        val entries = clientJwksRegistrationStore.getObject().load();
        return ResponseEntity.ok(entries);
    }

    /**
     * Load client JWKS entries.
     *
     * @return the response entity
     */
    @DeleteMapping(path = "/clients/{jkt}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Remove client JWKS entry by JKT")
    public ResponseEntity removeClientJwksEntry(@PathVariable final String jkt) {
        clientJwksRegistrationStore.getObject().removeByJkt(jkt);
        return ResponseEntity.noContent().build();
    }
}
