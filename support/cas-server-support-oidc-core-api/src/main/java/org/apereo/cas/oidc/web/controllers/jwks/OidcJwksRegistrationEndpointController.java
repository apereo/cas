package org.apereo.cas.oidc.web.controllers.jwks;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationNonceStore;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationRequest;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationResponse;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationStore;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;
import java.security.Signature;

/**
 * This is {@link OidcJwksRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@Tag(name = "OpenID Connect")
public class OidcJwksRegistrationEndpointController extends BaseOidcController {
    private final ClientJwksRegistrationNonceStore clientJwksRegistrationNonceStore;
    private final ClientJwksRegistrationStore clientJwksRegistrationStore;

    public OidcJwksRegistrationEndpointController(
        final OidcConfigurationContext configurationContext,
        final ClientJwksRegistrationNonceStore clientJwksRegistrationNonceStore,
        final ClientJwksRegistrationStore clientJwksRegistrationStore) {
        super(configurationContext);
        this.clientJwksRegistrationNonceStore = clientJwksRegistrationNonceStore;
        this.clientJwksRegistrationStore = clientJwksRegistrationStore;
    }

    @PostMapping('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.JWKS_URL + "/clients/register")
    public ResponseEntity handleRegistration(
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorization,
        @RequestBody final ClientJwksRegistrationRequest request) throws Throwable {

        val entry = clientJwksRegistrationNonceStore.find(request.nonceId());

        val jwk = JWK.parse(request.publicJwk());
        val jkt = jwk.computeThumbprint().toString();

        val signature = Base64.getUrlDecoder().decode(request.signature());
        val message = entry.nonce().getBytes(StandardCharsets.UTF_8);

        val ok = verifySignature(jwk, message, signature);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid signature");
        }
        clientJwksRegistrationNonceStore.remove(request.nonceId());
        clientJwksRegistrationStore.save(jkt, request.publicJwk());
        return ResponseEntity.ok(new ClientJwksRegistrationResponse(jkt));
    }

    @PostMapping('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.JWKS_URL + "/clients/challenge")
    public ResponseEntity handleChallenge(
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorization) {
        return ResponseEntity.ok(clientJwksRegistrationNonceStore.create());
    }

    protected boolean verifySignature(final JWK jwk, final byte[] message, final byte[] signature) throws Exception {
        switch (jwk) {
            case ECKey ecKey -> {
                return verifySignature(message, signature, ecKey.toPublicKey(), "SHA256withECDSA");
            }
            case RSAKey rsaKey -> {
                return verifySignature(message, signature, rsaKey.toRSAPublicKey(), "SHA256withRSA");
            }
            default -> throw new IllegalArgumentException("Unsupported key type: " + jwk.getKeyType());
        }
    }

    private static boolean verifySignature(final byte[] message, final byte[] signature,
                                           final PublicKey publicKey, final String algorithm) throws Exception {
        val sig = Signature.getInstance(algorithm);
        sig.initVerify(publicKey);
        sig.update(message);
        return sig.verify(signature);
    }
}
