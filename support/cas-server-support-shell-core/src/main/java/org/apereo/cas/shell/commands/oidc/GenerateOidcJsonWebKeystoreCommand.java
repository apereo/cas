package org.apereo.cas.shell.commands.oidc;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.generator.OidcDefaultJsonWebKeystoreGeneratorService;
import org.apereo.cas.shell.commands.CasShellCommand;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

/**
 * This is {@link GenerateOidcJsonWebKeystoreCommand}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class GenerateOidcJsonWebKeystoreCommand implements CasShellCommand {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    /**
     * Generate.
     *
     * @param jwksFile    the jwks file
     * @param jwksKeyId   the jwks key id
     * @param jwksKeySize the jwks key size
     * @param jwksKeyType the jwks key type
     * @throws Exception the exception
     */
    @Command(group = "OIDC", name = "generate-oidc-jwks", description = "Generate OIDC JSON Web Keystore")
    public void generate(
        @Option(
            longName = "jwksFile",
            description = "Location of the JSON web keystore file.",
            defaultValue = "/etc/cas/config/keystore.jwks"
        )
        final String jwksFile,

        @Option(
            longName = "jwksKeyId",
            description = "The key identifier to set for the generated key in the keystore.",
            defaultValue = "cas"
        )
        final String jwksKeyId,

        @Option(
            longName = "jwksKeySize",
            description = "The key size (algorithm-specific) for the generated jwks.",
            defaultValue = "2048"
        )
        final Integer jwksKeySize,

        @Option(
            longName = "jwksKeyType",
            description = "The type of the JWKS used to handle signing/encryption of authentication tokens.",
            defaultValue = "RSA"
        )
        final String jwksKeyType
    ) throws Exception {
        val properties = new OidcProperties();
        properties.getJwks().getCore().setJwksKeyId(jwksKeyId);
        properties.getJwks().getCore().setJwksKeySize(jwksKeySize);
        properties.getJwks().getCore().setJwksType(jwksKeyType);
        properties.getJwks().getFileSystem().setWatcherEnabled(false);
        properties.getJwks().getFileSystem().setJwksFile(jwksFile);
        val generator = new OidcDefaultJsonWebKeystoreGeneratorService(properties, applicationContext);
        val result = generator.generate();
        LOGGER.info("Generated keystore at [{}]", result);
    }
}
