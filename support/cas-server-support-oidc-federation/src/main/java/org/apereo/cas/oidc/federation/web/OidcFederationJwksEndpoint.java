package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.federation.signature.OidcFederationJsonWebKeystoreService;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.nimbusds.jose.jwk.JWK;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Rotates and retires the OpenID Federation keys of CAS on demand, as the rotation and revocation schedules do.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Endpoint(id = "oidcFederationJwks", defaultAccess = Access.NONE)
public class OidcFederationJwksEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<OidcFederationJsonWebKeystoreService> keystoreService;

    public OidcFederationJwksEndpoint(final CasConfigurationProperties casProperties,
                                      final ConfigurableApplicationContext applicationContext,
                                      final ObjectProvider<OidcFederationJsonWebKeystoreService> keystoreService) {
        super(casProperties, applicationContext);
        this.keystoreService = keystoreService;
    }

    /**
     * The published and historical federation keys, public only.
     *
     * @return the keys
     * @throws Exception the exception
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Report the published and historical OpenID Federation keys")
    public ResponseEntity<Map<String, Object>> keys() throws Exception {
        val service = keystoreService.getObject();
        return ResponseEntity.ok(Map.of(
            "published", service.toJWKSet().toJSONObject(true),
            "historical", service.getHistoricalKeys().stream().map(JWK::toJSONObject).toList()));
    }

    /**
     * Rotate the federation keys.
     *
     * @return the keys published after the rotation
     * @throws Exception the exception
     */
    @PostMapping(path = "/rotate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rotate the OpenID Federation keys: the future key becomes current, and a new future key is published")
    public ResponseEntity<Map<String, Object>> rotate() throws Exception {
        return ResponseEntity.ok(keystoreService.getObject().rotate().toJSONObject(true));
    }

    /**
     * Retire the previous federation keys.
     *
     * @return the keys published after the revocation
     * @throws Exception the exception
     */
    @PostMapping(path = "/revoke", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retire the previous OpenID Federation keys, which move to the historical keys")
    public ResponseEntity<Map<String, Object>> revoke() throws Exception {
        return ResponseEntity.ok(keystoreService.getObject().revoke().toJSONObject(true));
    }

    /**
     * Revoke a federation key, such as one whose private key is believed compromised.
     *
     * @param keyId  the key identifier
     * @param reason the reason of the revocation
     * @return the keys published after the revocation, or 404 when no such key is known
     * @throws Exception the exception
     */
    @PostMapping(path = "/revoke/{kid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Revoke an OpenID Federation key, which is published as a revoked historical key",
        parameters = {
            @Parameter(name = "kid", description = "The key identifier", required = true, in = ParameterIn.PATH),
            @Parameter(name = "reason", description = "The reason: compromised, superseded, unspecified or a reason of the federation",
                in = ParameterIn.QUERY)
        })
    public ResponseEntity<Map<String, Object>> revokeKey(@PathVariable("kid") final String keyId,
                                                         @RequestParam(value = "reason", required = false) final String reason)
            throws Exception {
        return keystoreService.getObject().revokeKey(keyId, reason)
            .map(keys -> ResponseEntity.ok(keys.toJSONObject(true)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
