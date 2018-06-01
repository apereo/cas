package org.apereo.cas.oidc.jwks;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;

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

    private final OidcProperties oidcProperties;

    /**
     * Generate.
     */
    @SneakyThrows
    public void generate() {
        final File file = oidcProperties.getJwksFile().getFile();
        generate(file, DEFAULT_KEYSTORE_BITS);
    }

    /**
     * Generate.
     *
     * @param file the file
     */
    public void generate(final File file) {
        generate(file, DEFAULT_KEYSTORE_BITS);
    }

    /**
     * Generate.
     *
     * @param file the file
     * @param bits the bits
     */
    @SneakyThrows
    protected void generate(final File file, final int bits) {
        if (!file.exists()) {
            final RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(bits);
            final JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(rsaJsonWebKey);
            final String data = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
            FileUtils.write(file, data, StandardCharsets.UTF_8);
            LOGGER.debug("Generated JSON web keystore at [{}]", file);
        } else {
            LOGGER.debug("Located JSON web keystore at [{}]", file);
        }
    }
}
