package org.apereo.cas.shell.commands;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.shell.cli.CommandLineOptions;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RegexUtils;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

/**
 * This is {@link GenerateKeyCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Service
public class GenerateKeyCommand implements CommandMarker {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateKeyCommand.class);

    /**
     * Find property.
     *
     * @param name the name
     */
    @CliCommand(value = "generate-key", help = "Generate keys for CAS settings")
    public void generateKey(
            @CliOption(key = {"name"},
                    help = "Property name for that holds the key",
                    optionContext = "Property name for that holds the key",
                    mandatory = true) final String name) {
        
        /*
        Because the command is used both from the shell and CLI,
        we need to validate parameters again.
         */
        if (StringUtils.isBlank(name)) {
            LOGGER.warn("No property/setting name is specified for key generation.");
            return;
        }

        final CasConfigurationMetadataRepository repository =
                new CasConfigurationMetadataRepository(
                        "file:/Users/Misagh/Workspace/GitWorkspace/cas-server/core/cas-server-core-configuration/build/classes/java/main/META-INF/spring" +
                                "-configuration-metadata.json");
        final String cryptoGroup = name.concat(".crypto");
        repository.getRepository().getAllGroups()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(cryptoGroup))
                .forEach(e -> {
                    final ConfigurationMetadataGroup grp = e.getValue();
                    grp.getSources().
                            forEach(Unchecked.biConsumer((k, v) -> {
                                final Object obj = ClassUtils.getClass(k, true).newInstance();
                                if (obj instanceof EncryptionJwtSigningJwtCryptographyProperties) {
                                    final EncryptionJwtSigningJwtCryptographyProperties crypto = (EncryptionJwtSigningJwtCryptographyProperties) obj;
                                    LOGGER.info(cryptoGroup.concat(".encryption.key=").concat(EncodingUtils.generateJsonWebKey(crypto.getEncryption().getKeySize())));
                                    LOGGER.info(cryptoGroup.concat(".signing.key=").concat(EncodingUtils.generateJsonWebKey(crypto.getSigning().getKeySize())));
                                } else if (obj instanceof EncryptionRandomizedSigningJwtCryptographyProperties) {
                                    final EncryptionRandomizedSigningJwtCryptographyProperties crypto = (EncryptionRandomizedSigningJwtCryptographyProperties) obj;
                                    LOGGER.info(cryptoGroup.concat(".encryption.key=").concat(RandomStringUtils.randomAlphanumeric(crypto.getEncryption().getKeySize())));
                                    LOGGER.info(cryptoGroup.concat(".signing.key=").concat(EncodingUtils.generateJsonWebKey(crypto.getSigning().getKeySize())));
                                }
                            }));
                });
    }
}
