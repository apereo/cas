package org.apereo.cas.oidc.jwks;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.util.ResourceUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJwkGenerator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link OidcJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcJsonWebKeystoreGeneratorService {
    private static final int DEFAULT_KEYSTORE_BITS = 2048;
    private static final File DEFAULT_JWKS_LOCATION = new File("/etc/cas/config/oidc-keystore.jwks");

    private final OidcProperties oidcProperties;

    /**
     * Generate.
     */
    @SneakyThrows
    public void generate() {
        generate(oidcProperties.getJwksFile(), DEFAULT_KEYSTORE_BITS);
    }

    /**
     * Generate.
     *
     * @param file the file
     */
    public void generate(final Resource file) {
        generate(file, DEFAULT_KEYSTORE_BITS);
    }

    /**
     * Generate.
     *
     * @param file the file
     * @param bits the bits
     */
    @SneakyThrows
    protected void generate(final Resource file, final int bits) {
        if (!ResourceUtils.doesResourceExist(file)) {
            val rsaJsonWebKey = RsaJwkGenerator.generateJwk(bits);
            val jsonWebKeySet = new JsonWebKeySet(rsaJsonWebKey);
            val data = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
            val location = (file instanceof FileSystemResource)
                ? FileSystemResource.class.cast(file).getFile()
                : DEFAULT_JWKS_LOCATION;
            FileUtils.write(location, data, StandardCharsets.UTF_8);
            LOGGER.debug("Generated JSON web keystore at [{}]", location);
        } else {
            LOGGER.debug("Located JSON web keystore at [{}]", file);
        }
    }
}
