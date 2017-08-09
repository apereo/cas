package org.apereo.cas.oidc.jwks;

import org.apache.commons.io.FileUtils;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link OidcJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcJsonWebKeystoreGeneratorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcJsonWebKeystoreGeneratorService.class);

    private final OidcProperties oidcProperties;

    public OidcJsonWebKeystoreGeneratorService(final OidcProperties oidcProperties) {
        this.oidcProperties = oidcProperties;
    }

    /**
     * Generate.
     */
    @PostConstruct
    public void generate() {
        try {
            final File file = oidcProperties.getJwksFile().getFile();
            if (!file.exists()) {
                final RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
                final JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(rsaJsonWebKey);
                final String data = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
                FileUtils.write(file, data, StandardCharsets.UTF_8);
                LOGGER.debug("Generated JSON web keystore at [{}]", file);
            } else {
                LOGGER.debug("Located JSON web keystore at [{}]", file);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
