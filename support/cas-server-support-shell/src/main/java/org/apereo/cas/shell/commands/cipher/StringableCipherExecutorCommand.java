package org.apereo.cas.shell.commands.cipher;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link StringableCipherExecutorCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ShellCommandGroup("Cipher Ops")
@ShellComponent
@Slf4j
public class StringableCipherExecutorCommand {

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
    @SneakyThrows
    @ShellMethod(key = {"cipher-text", "encode-text"}, value = "Sign and encrypt text data using keys")
    public String cipher(
        @ShellOption(value = { "value", "--value" }, defaultValue = ShellOption.NULL, help = "Value to put through the cipher")
        final String value,
        @ShellOption(value = { "encryption-key", "--encryption-key" }, defaultValue = ShellOption.NULL, help = "Encryption key")
        final String secretKeyEncryption,
        @ShellOption(value = { "signing-key", "--signing-key" }, defaultValue = ShellOption.NULL, help = "Signing key")
        final String secretKeySigning,
        @ShellOption(value = { "encryption-key-size", "--encryption-key-size" },
            defaultValue = StringUtils.EMPTY + CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE, help = "Encryption key size")
        final int secretKeyEncryptionSize,
        @ShellOption(value = { "signing-key-size", "--signing-key-size" },
            defaultValue = StringUtils.EMPTY + CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE, help = "Signing key size")
        final int secretKeySigningSize,
        @ShellOption(value = { "enable-encryption", "--enable-encryption" }, defaultValue = "true", help = "Whether value should be encrypted")
        final boolean encryptionEnabled,
        @ShellOption(value = { "enable-signing", "--enable-signing" }, defaultValue = "true", help = "Whether value should be signed")
        final boolean signingEnabled) {

        var toEncode = value;
        if (value != null && new File(value).exists()) {
            toEncode = FileUtils.readFileToString(new File(value), StandardCharsets.UTF_8);
        }

        if (StringUtils.isNotBlank(toEncode)) {
            val cipher = new ShellStringCipherExecutor(secretKeyEncryption, secretKeySigning,
                encryptionEnabled, signingEnabled, secretKeySigningSize, secretKeyEncryptionSize);
            val encoded = cipher.encode(toEncode);
            LOGGER.info("Encoded value: [{}]", encoded);
            return encoded;
        }
        return null;
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
    @SneakyThrows
    @ShellMethod(key = {"decipher-text", "decode-text"}, value = "Decrypt and verify text data using keys")
    public String decipher(
        @ShellOption(value = { "value", "--value" }, defaultValue = ShellOption.NULL, help = "Value to put through the cipher")
        final String value,
        @ShellOption(value = { "encryption-key", "--encryption-key" }, defaultValue = ShellOption.NULL, help = "Encryption key")
        final String secretKeyEncryption,
        @ShellOption(value = { "signing-key", "--signing-key" }, defaultValue = ShellOption.NULL, help = "Signing key")
        final String secretKeySigning,
        @ShellOption(value = { "encryption-key-size", "--encryption-key-size" },
            defaultValue = StringUtils.EMPTY + CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE, help = "Encryption key size")
        final int secretKeyEncryptionSize,
        @ShellOption(value = { "signing-key-size", "--signing-key-size" },
            defaultValue = StringUtils.EMPTY + CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE, help = "Signing key size")
        final int secretKeySigningSize,
        @ShellOption(value = { "enable-encryption", "--enable-encryption" }, defaultValue = "true", help = "Whether value should be encrypted")
        final boolean encryptionEnabled,
        @ShellOption(value = { "enable-signing", "--enable-signing" }, defaultValue = "true", help = "Whether value should be signed")
        final boolean signingEnabled) {

        var toEncode = value;
        if (value != null && new File(value).exists()) {
            toEncode = FileUtils.readFileToString(new File(value), StandardCharsets.UTF_8);
        }

        if (StringUtils.isNotBlank(toEncode)) {
            val cipher = new ShellStringCipherExecutor(secretKeyEncryption, secretKeySigning,
                encryptionEnabled, signingEnabled, secretKeySigningSize, secretKeyEncryptionSize);
            val decoded = cipher.decode(toEncode);
            LOGGER.info("Decoded value: [{}]", decoded);
            return decoded;
        }
        return null;
    }

    private static class ShellStringCipherExecutor extends BaseStringCipherExecutor {
        ShellStringCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                  final boolean encryptionEnabled, final boolean signingEnabled,
                                  final int signingKeySize, final int encryptionKeySize) {
            super(secretKeyEncryption, secretKeySigning, encryptionEnabled, signingEnabled, signingKeySize, encryptionKeySize);
        }
    }
}
