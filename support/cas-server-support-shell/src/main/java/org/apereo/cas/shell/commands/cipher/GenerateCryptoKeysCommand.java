package org.apereo.cas.shell.commands.cipher;

import org.apereo.cas.util.EncodingUtils;

import lombok.extern.slf4j.Slf4j;
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
     * @param keySize the key size
     */
    @ShellMethod(key = "generate-key", value = "Generate signing/encryption crypto keys for CAS settings")
    public void generateKey(@ShellOption(value = { "key-size", "--key-size" },
        defaultValue = "256", help = "Key size") final int keySize) {
        LOGGER.info(EncodingUtils.generateJsonWebKey(keySize));
    }
}
