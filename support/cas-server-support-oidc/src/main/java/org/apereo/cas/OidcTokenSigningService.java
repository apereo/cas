package org.apereo.cas;

import com.google.common.base.Throwables;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.OidcRegisteredService;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

/**
 * This is {@link OidcTokenSigningService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcTokenSigningService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcTokenSigningService.class);

    private final LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> cache;

    public OidcTokenSigningService(final LoadingCache<OidcRegisteredService, Optional<RsaJsonWebKey>> cache) {
        this.cache = cache;
    }

    /**
     * Sign id token claim string.
     *
     * @param svc    the service
     * @param claims the claims
     * @return the string
     * @throws JoseException the jose exception
     */
    public String signClaims(final OidcRegisteredService svc, final JwtClaims claims)
            throws JoseException {
        try {
            LOGGER.debug("Attempting to sign id token generated for service [{}]", svc);
            final JsonWebSignature jws = new JsonWebSignature();
            final String jsonClaims = claims.toJson();
            jws.setPayload(jsonClaims);
            LOGGER.debug("Generated claims are [{}]", jsonClaims);

            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.NONE);
            jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);

            final Optional<RsaJsonWebKey> jwks = cache.get(svc);
            if (svc.isSignIdToken() && !jwks.isPresent()) {
                throw new IllegalArgumentException("Service " + svc.getServiceId()
                        + " with client id " + svc.getClientId()
                        + " is configured to signn id tokens, yet no JSON web key is available");
            }

            if (svc.isSignIdToken()) {
                LOGGER.debug("Service [{}] is set to sign id tokens", svc);
                final RsaJsonWebKey jsonWebKey = jwks.get();
                LOGGER.debug("Found JSON web key to sign the id token: [{}]", jsonWebKey);
                if (jsonWebKey == null || jsonWebKey.getPrivateKey() == null) {
                    throw new IllegalArgumentException("JSON web key used to sign the id token has no associated private key");
                }

                jws.setKey(jsonWebKey.getPrivateKey());
                jws.setAlgorithmConstraints(AlgorithmConstraints.DISALLOW_NONE);
                if (StringUtils.isBlank(jsonWebKey.getKeyId())) {
                    jws.setKeyIdHeaderValue(UUID.randomUUID().toString());
                } else {
                    jws.setKeyIdHeaderValue(jsonWebKey.getKeyId());
                }
                LOGGER.debug("Signing id token with key id header value [{}]", jws.getKeyIdHeaderValue());
                jws.setAlgorithmHeaderValue(getJsonWebKeySigningAlgorithm());

                LOGGER.debug("Signing id token with algorithm [{}]", jws.getAlgorithmHeaderValue());
            }

            return jws.getCompactSerialization();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    public String getJsonWebKeySigningAlgorithm() {
        return AlgorithmIdentifiers.RSA_USING_SHA256;
    }
}
