package org.apereo.cas.shell.commands;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

/**
 * This is {@link GenerateCryptoKeysCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Service
public class GenerateCryptoKeysCommand implements CommandMarker {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateCryptoKeysCommand.class);

    /**
     * Generate key.
     *
     * @param name the name
     */
    @CliCommand(value = "generate-key", help = "Generate signing/encryption crypto keys for CAS settings")
    public void generateKey(
            @CliOption(key = {"group"},
                    help = "Property group that holds the key (i.e. cas.webflow). The group must have a child category of 'crypto'.",
                    mandatory = true,
                    specifiedDefaultValue = "",
                    unspecifiedDefaultValue = "",
                    optionContext = "Property name for that holds the key") final String name) {
        
        /*
        Because the command is used both from the shell and CLI,
        we need to validate parameters again.
         */
        if (StringUtils.isBlank(name)) {
            LOGGER.warn("No property/setting name is specified for signing/encryption key generation.");
            return;
        }

        final CasConfigurationMetadataRepository repository = new CasConfigurationMetadataRepository();
        final String cryptoGroup = name.concat(".crypto");
        repository.getRepository().getAllGroups()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(cryptoGroup))
                .forEach(e -> {
                    final ConfigurationMetadataGroup grp = e.getValue();
                    grp.getSources().forEach(Unchecked.biConsumer((k, v) -> {
                        final Object obj = ClassUtils.getClass(k, true).newInstance();
                        if (obj instanceof EncryptionJwtSigningJwtCryptographyProperties) {
                            final EncryptionJwtSigningJwtCryptographyProperties crypto = (EncryptionJwtSigningJwtCryptographyProperties) obj;
                            LOGGER.info(cryptoGroup.concat(".encryption.key="+EncodingUtils.generateJsonWebKey(crypto.getEncryption().getKeySize())));
                            LOGGER.info(cryptoGroup.concat(".signing.key="+EncodingUtils.generateJsonWebKey(crypto.getSigning().getKeySize())));
                        } else if (obj instanceof EncryptionRandomizedSigningJwtCryptographyProperties) {
                            final EncryptionRandomizedSigningJwtCryptographyProperties crypto = (EncryptionRandomizedSigningJwtCryptographyProperties) obj;
                            final String encKey = new Base64RandomStringGenerator(crypto.getEncryption().getKeySize()).getNewString();
                            LOGGER.info(cryptoGroup.concat(".encryption.key=" + encKey));
                            LOGGER.info(cryptoGroup.concat(".signing.key="+EncodingUtils.generateJsonWebKey(crypto.getSigning().getKeySize())));
                        }
                    }));
                });
    }
}
