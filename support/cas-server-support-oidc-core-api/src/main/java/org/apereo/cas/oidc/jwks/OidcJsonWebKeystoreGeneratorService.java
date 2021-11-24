package org.apereo.cas.oidc.jwks;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is {@link OidcJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface OidcJsonWebKeystoreGeneratorService {

    /**
     * To resource.
     *
     * @param jsonWebKeySet the json web key set
     * @return the resource
     */
    static Resource toResource(final JsonWebKeySet jsonWebKeySet) {
        val result = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        return new ByteArrayResource(result.getBytes(StandardCharsets.UTF_8), "OIDC JWKS");
    }

    /**
     * To json web key store.
     *
     * @param resource the resource
     * @return the json web key set
     * @throws Exception the exception
     */
    static JsonWebKeySet toJsonWebKeyStore(final Resource resource) throws Exception {
        val result = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        return new JsonWebKeySet(result);
    }

    /**
     * Generate json web key json web key.
     *
     * @param oidcProperties the oidc properties
     * @return the json web key
     */
    static JsonWebKey generateJsonWebKey(final OidcProperties oidcProperties) {
        val properties = oidcProperties.getJwks().getCore();
        val jsonWebKey = OidcJsonWebKeyStoreUtils.generateJsonWebKey(properties.getJwksType(), properties.getJwksKeySize());
        jsonWebKey.setKeyId(properties.getJwksKeyId().concat("-").concat(RandomUtils.randomAlphabetic(8)));
        return jsonWebKey;
    }

    /**
     * Generate key with state json web key.
     *
     * @param state          the state
     * @param oidcProperties the oidc properties
     * @return the json web key
     */
    static JsonWebKey generateJsonWebKey(final OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates state,
                                         final OidcProperties oidcProperties) {
        val key = OidcJsonWebKeystoreGeneratorService.generateJsonWebKey(oidcProperties);
        OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates.setJsonWebKeyState(key, state);
        return key;
    }

    /**
     * Generate keystore for OIDC.
     *
     * @return the resource
     * @throws Exception the exception
     */
    Resource generate() throws Exception;

    /**
     * Store json web key set.
     *
     * @param jsonWebKeySet the json web key set
     * @return the json web key set
     * @throws Exception the exception
     */
    JsonWebKeySet store(JsonWebKeySet jsonWebKeySet) throws Exception;

    /**
     * Find keystore resource for the issuer.
     *
     * @return the optional
     * @throws Exception the exception
     */
    Optional<Resource> find() throws Exception;
}
