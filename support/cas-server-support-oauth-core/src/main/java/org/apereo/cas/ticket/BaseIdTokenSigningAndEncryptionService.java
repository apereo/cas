package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.security.Key;

/**
 * This is {@link BaseIdTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public abstract class BaseIdTokenSigningAndEncryptionService implements IdTokenSigningAndEncryptionService {

    /**
     * Gets json web signature.
     *
     * @param claims the claims
     * @return the json web signature
     */
    protected JsonWebSignature createJsonWebSignature(final JwtClaims claims) {
        val jws = new JsonWebSignature();
        val jsonClaims = claims.toJson();
        jws.setPayload(jsonClaims);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.NONE);
        jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);
        return jws;
    }

    /**
     * Create json web encryption json web encryption.
     *
     * @param encryptionAlg      the encryption alg
     * @param encryptionEncoding the encryption encoding
     * @param keyIdHeaderValue   the key id header value
     * @param publicKey          the public key
     * @param payload            the payload
     * @return the json web encryption
     */
    @SneakyThrows
    protected String encryptIdToken(final String encryptionAlg,
                                               final String encryptionEncoding,
                                               final String keyIdHeaderValue,
                                               final Key publicKey,
                                               final String payload) {
        val jwe = new JsonWebEncryption();
        jwe.setAlgorithmHeaderValue(encryptionAlg);
        jwe.setEncryptionMethodHeaderParameter(encryptionEncoding);
        jwe.setKey(publicKey);
        jwe.setKeyIdHeaderValue(keyIdHeaderValue);
        jwe.setContentTypeHeaderValue("JWT");
        jwe.setPayload(payload);
        return jwe.getCompactSerialization();
    }

    /**
     * Configure json web signature for id token signing.
     *
     * @param svc        the svc
     * @param jws        the jws
     * @param jsonWebKey the json web key
     * @return the json web signature
     */
    protected JsonWebSignature configureJsonWebSignatureForIdTokenSigning(final OAuthRegisteredService svc,
                                                                          final JsonWebSignature jws,
                                                                          final RsaJsonWebKey jsonWebKey) {
        LOGGER.debug("Service [{}] is set to sign id tokens", svc);
        jws.setKey(jsonWebKey.getPrivateKey());
        jws.setAlgorithmConstraints(AlgorithmConstraints.DISALLOW_NONE);
        if (StringUtils.isNotBlank(jsonWebKey.getKeyId())) {
            jws.setKeyIdHeaderValue(jsonWebKey.getKeyId());
        }
        LOGGER.debug("Signing id token with key id header value [{}]", jws.getKeyIdHeaderValue());
        jws.setAlgorithmHeaderValue(getJsonWebKeySigningAlgorithm(svc));

        LOGGER.debug("Signing id token with algorithm [{}]", jws.getAlgorithmHeaderValue());
        return jws;
    }
}
