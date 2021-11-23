package org.apereo.cas.oidc.jwks;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.springframework.core.io.Resource;

/**
 * This is {@link OidcJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@FunctionalInterface
public interface OidcJsonWebKeystoreGeneratorService {

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
     */
    Resource generate();
}
