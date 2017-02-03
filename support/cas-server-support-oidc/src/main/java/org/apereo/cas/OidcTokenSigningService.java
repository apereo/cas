package org.apereo.cas;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.OidcRegisteredService;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
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

    @Autowired
    private ResourceLoader resourceLoader;

    private final Resource jwksFile;

    public OidcTokenSigningService(final Resource jwksFile) {
        this.jwksFile = jwksFile;
    }

    private RsaJsonWebKey getJsonSigningWebKeyFromJwks(final JsonWebKeySet jwks) {
        if (jwks.getJsonWebKeys().isEmpty()) {
            LOGGER.warn("No JSON web keys are available in the keystore");
            return null;
        }

        final RsaJsonWebKey key = (RsaJsonWebKey) jwks.getJsonWebKeys().get(0);
        if (StringUtils.isNotBlank(key.getAlgorithm())) {
            LOGGER.warn("Located JSON web key [{}] has no algorithm defined", key);
        }
        if (StringUtils.isNotBlank(key.getKeyId())) {
            LOGGER.warn("Located JSON web key [{}] has no key id defined", key);
        }

        if (key.getPrivateKey() == null) {
            LOGGER.warn("Located JSON web key [{}] has no private key", key);
            return null;
        }
        return key;
    }


    /**
     * Sign id token claim string.
     *
     * @param svc    the service
     * @param claims the claims
     * @return the string
     * @throws JoseException the jose exception
     */
    public String signIdTokenClaim(final OidcRegisteredService svc, final JwtClaims claims)
            throws JoseException {
        try {
            LOGGER.debug("Attempting to sign id token generated for service [{}]", svc);
            final JsonWebSignature jws = new JsonWebSignature();
            final String jsonClaims = claims.toJson();
            jws.setPayload(jsonClaims);
            LOGGER.debug("Generated claims are [{}]", jsonClaims);

            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.NONE);
            jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);

            final Optional<JsonWebKeySet> jwks = buildJsonWebKeySet(svc);
            if (svc.isSignIdToken() && (!jwks.isPresent() || jwks.get().getJsonWebKeys().isEmpty())) {
                throw new IllegalArgumentException("Service " + svc.getServiceId()
                        + " with client id " + svc.getClientId()
                        + " is configured to signn id tokens, yet no JSON web key is available");
            }

            if (svc.isSignIdToken()) {
                LOGGER.debug("Service [{}] is set to sign id tokens", svc);

                final RsaJsonWebKey jsonWebKey = getJsonSigningWebKeyFromJwks(jwks.get());
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
                jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

                LOGGER.debug("Signing id token with algorithm [{}]", jws.getAlgorithmHeaderValue());
            }

            return jws.getCompactSerialization();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    private JsonWebKeySet buildJsonWebKeySet(final Resource resource) throws Exception {
        final String json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.debug("Retrieved JSON web key from [{}] as [{}]", resource, json);
        return buildJsonWebKeySet(json);
    }

    private JsonWebKeySet buildJsonWebKeySet(final String json) throws Exception {
        final JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(json);
        final RsaJsonWebKey webKey = getJsonSigningWebKeyFromJwks(jsonWebKeySet);
        if (webKey == null || webKey.getPrivateKey() == null) {
            LOGGER.warn("JSON web key retrieved [{}] is not found or has no associated private key", webKey);
            return null;
        }
        return jsonWebKeySet;
    }

    /**
     * Build json web key set.
     *
     * @param service the service
     * @return the json web key set
     * @throws Exception the exception
     */
    private Optional<JsonWebKeySet> buildJsonWebKeySet(final OidcRegisteredService service) throws Exception {
        JsonWebKeySet jsonWebKeySet = null;
        try {
            if (StringUtils.isNotBlank(service.getJwks())) {
                LOGGER.debug("Loading JSON web key from [{}]", service.getJwks());
                final Resource resource = this.resourceLoader.getResource(service.getJwks());
                jsonWebKeySet = buildJsonWebKeySet(resource);
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        } finally {
            if (jsonWebKeySet == null) {
                LOGGER.debug("Loading default JSON web key from [{}]", this.jwksFile);
                if (this.jwksFile != null) {
                    LOGGER.debug("Retrieving default JSON web key from [{}]", this.jwksFile);
                    jsonWebKeySet = buildJsonWebKeySet(this.jwksFile);
                }
            }
        }
        if (jsonWebKeySet == null || jsonWebKeySet.getJsonWebKeys().isEmpty()) {
            LOGGER.warn("No JSON web keys could be found for [{}]", service);
            return Optional.empty();
        }

        final long badKeysCount = jsonWebKeySet.getJsonWebKeys().stream().filter(k ->
                StringUtils.isNotBlank(k.getAlgorithm())
                        && StringUtils.isNotBlank(k.getKeyId())
                        && StringUtils.isNotBlank(k.getKeyType())).count();

        if (badKeysCount == jsonWebKeySet.getJsonWebKeys().size()) {
            LOGGER.warn("No valid JSON web keys could be found for [{}]", service);
            return Optional.empty();
        }

        final RsaJsonWebKey webKey = getJsonSigningWebKeyFromJwks(jsonWebKeySet);
        if (webKey.getPrivateKey() == null) {
            LOGGER.warn("JSON web key retrieved [{}] has no associated private key", webKey.getKeyId());
            return Optional.empty();
        }
        return Optional.of(jsonWebKeySet);
    }
}
