package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link OidcDefaultJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcDefaultJsonWebKeystoreGeneratorService implements OidcJsonWebKeystoreGeneratorService {

    private final OidcProperties oidcProperties;

    @SneakyThrows
    @Override
    public Resource generate() {
        val resolve = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(oidcProperties.getJwks().getJwksFile());
        val resource = ResourceUtils.getRawResourceFrom(resolve);
        return generate(resource);
    }

    /**
     * Generate.
     *
     * @param file the file
     * @return the resource
     */
    @SneakyThrows
    protected Resource generate(final Resource file) {
        if (ResourceUtils.doesResourceExist(file)) {
            LOGGER.debug("Located JSON web keystore at [{}]", file);
            return file;
        }
        val jwk = generateJsonWebKey();
        val data = new JsonWebKeySet(jwk).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        val location = file.getFile();
        FileUtils.write(location, data, StandardCharsets.UTF_8);
        LOGGER.debug("Generated JSON web keystore at [{}]", location);
        return file;
    }

    /**
     * Generate json web key public json web key.
     *
     * @return the public json web key
     */
    @SneakyThrows
    protected PublicJsonWebKey generateJsonWebKey() {
        val jwks = oidcProperties.getJwks();
        return OidcJsonWebKeyStoreUtils.generateJsonWebKey(jwks.getJwksType(), jwks.getJwksKeySize());
    }
}
