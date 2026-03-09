package org.apereo.cas.oidc.web.controllers.jwks;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationRequest;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationResponse;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationStore;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcJwksRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@Tag(name = "OpenID Connect")
public class OidcJwksRegistrationEndpointController extends BaseOidcController {
    private final ClientJwksRegistrationStore clientJwksRegistrationStore;

    public OidcJwksRegistrationEndpointController(
        final OidcConfigurationContext configurationContext,
        final ClientJwksRegistrationStore clientJwksRegistrationStore) {
        super(configurationContext);
        this.clientJwksRegistrationStore = clientJwksRegistrationStore;
    }

    /**
     * Handle registration.
     *
     * @param request the request
     * @return the response entity
     * @throws Throwable the throwable
     */
    @PostMapping({
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.JWKS_URL + "/clients/register",
        "/**/" + OidcConstants.JWKS_URL + "/clients/register"
    })
    @Operation(summary = "Handle client JWKS registration request",
        description = "This endpoint allows clients to register their JSON Web Keys (JWKs) for use in OpenID Connect operations",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                schema = @Schema(implementation = ClientJwksRegistrationRequest.class)
            )
        ))
    public ResponseEntity handleRegistration(
        final HttpServletRequest request,
        @RequestBody final ClientJwksRegistrationRequest registrationRequest) throws Throwable {
        val accessToken = getAccessTokenFromRequest(request).getValue();
        val accessTokenTicket = FunctionUtils.doAndHandle(() -> {
            val decodedToken = getConfigurationContext().getTicketRegistry().getTicket(accessToken, OAuth20AccessToken.class);
            return decodedToken == null || decodedToken.isExpired() ? null : decodedToken;
        });
        if (accessTokenTicket == null
            || accessTokenTicket.isExpired()
            || !accessTokenTicket.getScopes().contains(OidcConstants.CLIENT_JWKS_REGISTRATION_SCOPE)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token");
        }

        val jws = JWSObject.parse(registrationRequest.proof());
        val jwk = jws.getHeader().getJWK();
        val jkt = jwk.computeThumbprint().toString();

        val alg = jws.getHeader().getAlgorithm();
        FunctionUtils.throwIf(!JWSAlgorithm.Family.EC.contains(alg) && !JWSAlgorithm.Family.RSA.contains(alg) && !JWSAlgorithm.EdDSA.equals(alg),
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid algorithm: " + alg));
        
        val verifier = switch (jwk) {
            case ECKey ecKey -> new ECDSAVerifier(ecKey);
            case RSAKey rsaKey -> new RSASSAVerifier(rsaKey);
            case OctetKeyPair okp -> new Ed25519Verifier(okp.toPublicJWK());
            default -> throw new IllegalArgumentException("Unsupported key type: " + jwk.getKeyType());
        };
        if (!jws.verify((JWSVerifier) verifier)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid signature");
        }
        clientJwksRegistrationStore.save(jkt, jwk.toPublicJWK().toJSONString());
        return ResponseEntity.ok(new ClientJwksRegistrationResponse(jkt));
    }

    /**
     * Handle errors.
     *
     * @param ex the ex
     * @return the response entity
     */
    @ExceptionHandler(Exception.class)
    @SuppressWarnings("UnusedMethod")
    private static ResponseEntity<String> handle(final Exception ex) {
        LoggingUtils.error(LOGGER, ex);
        if (ex instanceof final ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(rse.getReason());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
