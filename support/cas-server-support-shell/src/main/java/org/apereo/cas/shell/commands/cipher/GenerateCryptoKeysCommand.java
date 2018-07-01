package org.apereo.cas.shell.commands.cipher;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import org.jooq.lambda.Unchecked;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * This is {@link GenerateCryptoKeysCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ShellCommandGroup("Cipher Ops")
@ShellComponent
@Slf4j
public class GenerateCryptoKeysCommand {

    /**
     * Generate key.
     *
     * @param name the name
     */
    @ShellMethod(key = "generate-key", value = "Generate signing/encryption crypto keys for CAS settings")
    public void generateKey(
        @ShellOption(value = {"group"},
            help = "Property group that holds the key (i.e. cas.webflow). The group must have a child category of 'crypto'.") final String name) {
        
        /*
        Because the command is used both from the shell and CLI,
        we need to validate parameters again.
         */
        if (StringUtils.isBlank(name)) {
            LOGGER.warn("No property/setting name is specified for signing/encryption key generation.");
            return;
        }

        final var repository = new CasConfigurationMetadataRepository();
        final var cryptoGroup = name.concat(".crypto");
        repository.getRepository().getAllGroups()
            .entrySet()
            .stream()
            .filter(e -> e.getKey().startsWith(cryptoGroup))
            .forEach(e -> {
                final var grp = e.getValue();
                grp.getSources().forEach(Unchecked.biConsumer((k, v) -> {
                    final Object obj = ClassUtils.getClass(k, true).getDeclaredConstructor().newInstance();
                    if (obj instanceof EncryptionJwtSigningJwtCryptographyProperties) {
                        final var crypto = (EncryptionJwtSigningJwtCryptographyProperties) obj;
                        LOGGER.info(cryptoGroup.concat(".encryption.key=" + EncodingUtils.generateJsonWebKey(crypto.getEncryption().getKeySize())));
                        LOGGER.info(cryptoGroup.concat(".signing.key=" + EncodingUtils.generateJsonWebKey(crypto.getSigning().getKeySize())));
                    } else if (obj instanceof EncryptionRandomizedSigningJwtCryptographyProperties) {
                        final var crypto = (EncryptionRandomizedSigningJwtCryptographyProperties) obj;
                        final var encKey = new Base64RandomStringGenerator(crypto.getEncryption().getKeySize()).getNewString();
                        LOGGER.info(cryptoGroup.concat(".encryption.key=" + encKey));
                        LOGGER.info(cryptoGroup.concat(".signing.key=" + EncodingUtils.generateJsonWebKey(crypto.getSigning().getKeySize())));
                    }
                }));
            });
    }
}
