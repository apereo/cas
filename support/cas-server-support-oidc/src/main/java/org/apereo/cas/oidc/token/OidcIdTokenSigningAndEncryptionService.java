package org.apereo.cas.oidc.token;

import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.OidcRegisteredService;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This is {@link OidcIdTokenSigningAndEncryptionService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcIdTokenSigningAndEncryptionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcIdTokenSigningAndEncryptionService.class);

    private final LoadingCache<String, Optional<RsaJsonWebKey>> defaultJsonWebKeystoreCache;
    private final LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache;

    private final String issuer;

    public OidcIdTokenSigningAndEncryptionService(final LoadingCache<String, Optional<RsaJsonWebKey>> defaultJsonWebKeystoreCache,
                                                  final LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> serviceJsonWebKeystoreCache,
                                                  final String issuer) {
        this.defaultJsonWebKeystoreCache = defaultJsonWebKeystoreCache;
        this.serviceJsonWebKeystoreCache = serviceJsonWebKeystoreCache;
        this.issuer = issuer;
    }

    /**
     * Sign id token claim string.
     *
     * @param svc    the service
     * @param claims the claims
     * @return the string
     */
    public String encode(final OidcRegisteredService svc, final JwtClaims claims) {
        try {
            LOGGER.debug("Attempting to produce id token generated for service [{}]", svc);
            final JsonWebSignature jws = new JsonWebSignature();
            final String jsonClaims = claims.toJson();
            jws.setPayload(jsonClaims);
            LOGGER.debug("Generated claims to put into id token are [{}]", jsonClaims);

            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.NONE);
            jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);

            String innerJwt = svc.isSignIdToken() ? signIdToken(svc, jws) : jws.getCompactSerialization();
            if (svc.isEncryptIdToken() && StringUtils.isNotBlank(svc.getIdTokenEncryptionAlg())
                    && StringUtils.isNotBlank(svc.getIdTokenEncryptionEncoding())) {
                innerJwt = encryptIdToken(svc, jws, innerJwt);
            }

            return innerJwt;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String encryptIdToken(final OidcRegisteredService svc, final JsonWebSignature jws, final String innerJwt) throws Exception {
        LOGGER.debug("Service [{}] is set to encrypt id tokens", svc);
        final JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setAlgorithmHeaderValue(svc.getIdTokenEncryptionAlg());
        jwe.setEncryptionMethodHeaderParameter(svc.getIdTokenEncryptionEncoding());

        final Optional<RsaJsonWebKey> jwks = this.serviceJsonWebKeystoreCache.get(svc);
        if (!jwks.isPresent()) {
            throw new IllegalArgumentException("Service " + svc.getServiceId()
                    + " with client id " + svc.getClientId()
                    + " is configured to encrypt id tokens, yet no JSON web key is available");
        }
        final RsaJsonWebKey jsonWebKey = jwks.get();
        LOGGER.debug("Found JSON web key to encrypt the id token: [{}]", jsonWebKey);
        if (jsonWebKey.getPublicKey() == null) {
            throw new IllegalArgumentException("JSON web key used to sign the id token has no associated public key");
        }

        jwe.setKey(jsonWebKey.getPublicKey());
        jwe.setKeyIdHeaderValue(jws.getKeyIdHeaderValue());
        jwe.setContentTypeHeaderValue("JWT");
        jwe.setPayload(innerJwt);
        return jwe.getCompactSerialization();
    }

    private String signIdToken(final OidcRegisteredService svc, final JsonWebSignature jws) throws Exception {
        final Optional<RsaJsonWebKey> jwks = defaultJsonWebKeystoreCache.get(this.issuer);
        if (!jwks.isPresent()) {
            throw new IllegalArgumentException("Service " + svc.getServiceId()
                    + " with client id " + svc.getClientId()
                    + " is configured to sign id tokens, yet no JSON web key is available");
        }
        final RsaJsonWebKey jsonWebKey = jwks.get();
        LOGGER.debug("Found JSON web key to sign the id token: [{}]", jsonWebKey);
        if (jsonWebKey.getPrivateKey() == null) {
            throw new IllegalArgumentException("JSON web key used to sign the id token has no associated private key");
        }
        prepareJsonWebSignatureForIdTokenSigning(svc, jws, jsonWebKey);
        return jws.getCompactSerialization();
    }

    private void prepareJsonWebSignatureForIdTokenSigning(final OidcRegisteredService svc, final JsonWebSignature jws,
                                                          final RsaJsonWebKey jsonWebKey) {
        LOGGER.debug("Service [{}] is set to sign id tokens", svc);

        jws.setKey(jsonWebKey.getPrivateKey());
        jws.setAlgorithmConstraints(AlgorithmConstraints.DISALLOW_NONE);
        if (StringUtils.isNotBlank(jsonWebKey.getKeyId())) {
            jws.setKeyIdHeaderValue(jsonWebKey.getKeyId());
        }
        LOGGER.debug("Signing id token with key id header value [{}]", jws.getKeyIdHeaderValue());
        jws.setAlgorithmHeaderValue(getJsonWebKeySigningAlgorithm());

        LOGGER.debug("Signing id token with algorithm [{}]", jws.getAlgorithmHeaderValue());
    }

    public String getJsonWebKeySigningAlgorithm() {
        return AlgorithmIdentifiers.RSA_USING_SHA256;
    }
}
