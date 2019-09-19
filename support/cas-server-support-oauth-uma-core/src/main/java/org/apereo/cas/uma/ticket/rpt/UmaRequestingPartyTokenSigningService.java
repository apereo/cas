package org.apereo.cas.uma.ticket.rpt;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.BaseTokenSigningAndEncryptionService;
import org.apereo.cas.util.ResourceUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link UmaRequestingPartyTokenSigningService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class UmaRequestingPartyTokenSigningService extends BaseTokenSigningAndEncryptionService {
    private final RsaJsonWebKey jsonWebKey;

    @SneakyThrows
    public UmaRequestingPartyTokenSigningService(final Resource jwksFile, final String issuer) {
        super(issuer);
        if (ResourceUtils.doesResourceExist(jwksFile)) {
            val json = IOUtils.toString(jwksFile.getInputStream(), StandardCharsets.UTF_8);
            val jsonWebKeySet = new JsonWebKeySet(json);
            val keys = jsonWebKeySet.getJsonWebKeys();
            if (keys.isEmpty()) {
                throw new IllegalArgumentException("No JSON web keys are found in the JWKS keystore " + jwksFile);
            }
            this.jsonWebKey = RsaJsonWebKey.class.cast(jsonWebKeySet.getJsonWebKeys().get(0));
        } else {
            LOGGER.warn("JWKS file for UMA RPT tokens is undefined or cannot be located. Tokens will not be signed");
            this.jsonWebKey = null;
        }
    }

    @Override
    @SneakyThrows
    public String encode(final OAuthRegisteredService service, final JwtClaims claims) {
        LOGGER.debug("Generated claims to put into token are [{}]", claims.toJson());
        return signToken(service, claims, jsonWebKey);
    }

    @Override
    protected PublicJsonWebKey getJsonWebKeySigningKey() {
        return jsonWebKey;
    }
}
