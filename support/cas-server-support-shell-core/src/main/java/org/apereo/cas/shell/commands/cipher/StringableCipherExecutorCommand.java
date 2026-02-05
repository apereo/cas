package org.apereo.cas.shell.commands.cipher;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.shell.commands.CasShellCommand;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

/**
 * This is {@link StringableCipherExecutorCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class StringableCipherExecutorCommand implements CasShellCommand {

    /**
     * Cipher text.
     *
     * @param value                   the value
     * @param secretKeyEncryption     the secret key encryption
     * @param secretKeySigning        the secret key signing
     * @param secretKeyEncryptionSize the secret key encryption size
     * @param secretKeySigningSize    the secret key signing size
     * @param encryptionEnabled       the encryption enabled
     * @param signingEnabled          the signing enabled
     * @return the string
     */
    @Command(group = "Cipher", name = {"cipher-text", "encode-text"}, description = "Sign and encrypt text data using keys")
    public String cipher(
        @Option(longName = "value", description = "Value to put through the cipher")
        final String value,

        @Option(longName = "secretKeyEncryption", description = "Encryption key")
        final String secretKeyEncryption,

        @Option(
            longName = "encryptionAlg",
            defaultValue = EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM,
            description = "Encryption alg"
        )
        final String encryptionAlg,

        @Option(longName = "secretKeySigning", description = "Signing key")
        final String secretKeySigning,

        @Option(
            longName = "secretKeyEncryptionSize",
            defaultValue = StringUtils.EMPTY + EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE,
            description = "Encryption key size"
        )
        final Integer secretKeyEncryptionSize,

        @Option(
            longName = "secretKeySigningSize",
            defaultValue = StringUtils.EMPTY + SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE,
            description = "Signing key size"
        )
        final Integer secretKeySigningSize,

        @Option(longName = "encryptionEnabled", defaultValue = "true", description = "Whether value should be encrypted")
        final Boolean encryptionEnabled,

        @Option(longName = "signingEnabled", defaultValue = "true", description = "Whether value should be signed")
        final Boolean signingEnabled
    ) {

        var toEncode = value;
        if (value != null && new File(value).exists()) {
            toEncode = FunctionUtils.doUnchecked(() -> FileUtils.readFileToString(new File(value), StandardCharsets.UTF_8));
        }

        if (StringUtils.isNotBlank(toEncode)) {
            val cipher = new ShellStringCipherExecutor(secretKeyEncryption, secretKeySigning,
                encryptionEnabled, signingEnabled, secretKeySigningSize, secretKeyEncryptionSize);
            cipher.setContentEncryptionAlgorithmIdentifier(encryptionAlg);
            val encoded = cipher.encode(toEncode);
            LOGGER.info("Encoded value: [{}]", encoded);
            return encoded;
        }
        return null;
    }

    /**
     * Generate key.
     *
     * @param keySize the key size
     * @return the string
     */
    @Command(group = "Cipher", name = "generate-key", description = "Generate signing/encryption crypto keys for CAS settings")
    public String generateKey(
        @Option(longName = "key-size", defaultValue = "256", description = "Key size")
        final Integer keySize) {
        val key = EncodingUtils.generateJsonWebKey(keySize);
        LOGGER.info(key);
        return key;
    }

    /**
     * Decipher.
     *
     * @param value                   the value
     * @param secretKeyEncryption     the secret key encryption
     * @param secretKeySigning        the secret key signing
     * @param secretKeyEncryptionSize the secret key encryption size
     * @param secretKeySigningSize    the secret key signing size
     * @param encryptionEnabled       the encryption enabled
     * @param signingEnabled          the signing enabled
     * @return the string
     */
    @Command(group = "Cipher", name = {"decipher-text", "decode-text"}, description = "Decrypt and verify text data using keys")
    public String decipher(
        @Option(longName = "value", description = "Value to put through the cipher")
        final String value,

        @Option(longName = "secretKeyEncryption", description = "Encryption key")
        final String secretKeyEncryption,

        @Option(
            longName = "encryptionAlg",
            defaultValue = EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM,
            description = "Encryption alg"
        )
        final String encryptionAlg,

        @Option(longName = "secretKeySigning", description = "Signing key")
        final String secretKeySigning,

        @Option(
            longName = "secretKeyEncryptionSize",
            defaultValue = StringUtils.EMPTY + EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE,
            description = "Encryption key size"
        )
        final int secretKeyEncryptionSize,

        @Option(
            longName = "secretKeySigningSize",
            defaultValue = StringUtils.EMPTY + SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE,
            description = "Signing key size"
        )
        final Integer secretKeySigningSize,

        @Option(longName = "encryptionEnabled", defaultValue = "true", description = "Whether value should be encrypted")
        final Boolean encryptionEnabled,

        @Option(longName = "signingEnabled", defaultValue = "true", description = "Whether value should be signed")
        final Boolean signingEnabled) {

        var toEncode = value;
        if (value != null && new File(value).exists()) {
            toEncode = FunctionUtils.doUnchecked(() -> FileUtils.readFileToString(new File(value), StandardCharsets.UTF_8));
        }

        if (StringUtils.isNotBlank(toEncode)) {
            val cipher = new ShellStringCipherExecutor(secretKeyEncryption, secretKeySigning,
                encryptionEnabled, signingEnabled, secretKeySigningSize, secretKeyEncryptionSize);
            cipher.setContentEncryptionAlgorithmIdentifier(encryptionAlg);
            val decoded = cipher.decode(toEncode);
            LOGGER.info("Decoded value: [{}]", decoded);
            return decoded;
        }
        return null;
    }

    private static final class ShellStringCipherExecutor extends BaseStringCipherExecutor {
        ShellStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                  final boolean encryptionEnabled, final boolean signingEnabled,
                                  final int signingKeySize, final int encryptionKeySize) {
            super(secretKeyEncryption, secretKeySigning, encryptionEnabled,
                signingEnabled, signingKeySize, encryptionKeySize);
        }
    }
}
