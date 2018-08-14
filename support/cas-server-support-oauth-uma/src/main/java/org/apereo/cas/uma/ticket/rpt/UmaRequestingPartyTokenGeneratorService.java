package org.apereo.cas.uma.ticket.rpt;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.BaseIdTokenSigningAndEncryptionService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link UmaRequestingPartyTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class UmaRequestingPartyTokenGeneratorService extends BaseIdTokenSigningAndEncryptionService {
    private final RsaJsonWebKey jsonWebKey;

    @SneakyThrows
    public UmaRequestingPartyTokenGeneratorService(final Resource jwksFile) {
        val json = IOUtils.toString(jwksFile.getInputStream(), StandardCharsets.UTF_8);
        val jsonWebKeySet = new JsonWebKeySet(json);
        val keys = jsonWebKeySet.getJsonWebKeys();
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("No keys are found in the JWKS keystore " + jwksFile);
        }
        this.jsonWebKey = RsaJsonWebKey.class.cast(jsonWebKeySet.getJsonWebKeys().get(0));
    }

    @Override
    @SneakyThrows
    public String encode(final OAuthRegisteredService svc, final JwtClaims claims) {
        LOGGER.debug("Generated claims to put into token are [{}]", claims.toJson());
        val jws = createJsonWebSignature(claims);
        configureJsonWebSignatureForIdTokenSigning(svc, jws, jsonWebKey);
        return jws.getCompactSerialization();
    }
}
