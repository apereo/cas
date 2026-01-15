package org.apereo.cas.shell.commands.jasypt;

import module java.base;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.shell.commands.CasShellCommand;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.jasypt.iv.NoIvGenerator;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

/**
 * This is {@link JasyptDecryptPropertyCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JasyptDecryptPropertyCommand implements CasShellCommand {
    @Autowired
    private Environment environment;

    /**
     * Decrypt a value using Jasypt.
     *
     * @param value      the value
     * @param alg        the alg
     * @param provider   the provider
     * @param password   the password
     * @param initVector the init vector
     * @param iterations the iterations- defaults to {@value StandardPBEByteEncryptor#DEFAULT_KEY_OBTENTION_ITERATIONS}
     */
    @Command(name = "decrypt-value", description = "Decrypt a CAS property value/setting via Jasypt")
    public void decryptValue(
        @Option(
            longName = "value",
            description = "Value to decrypt"
        )
        final String value,

        @Option(
            longName = "alg",
            description = "Algorithm to use to decrypt"
        )
        final String alg,

        @Option(
            longName = "provider",
            description = "Security provider to use to decrypt"
        )
        final String provider,

        @Option(
            longName = "password",
            description = "Password (encryption key) to decrypt"
        )
        final String password,

        @Option(
            longName = "initVector",
            description = "Use initialization vector to encrypt",
            defaultValue = "false"
        )
        final Boolean initVector,

        @Option(
            longName = "iterations",
            description = "Key obtention iterations to decrypt, default 1000"
        )
        final String iterations
    ) {

        val cipher = new CasConfigurationJasyptCipherExecutor(this.environment);
        cipher.setAlgorithm(alg);
        cipher.setPassword(password);
        cipher.setProviderName(provider);
        cipher.setKeyObtentionIterations(iterations);
        cipher.setIvGenerator(initVector ? new RandomIvGenerator() : new NoIvGenerator());
        val decrypted = cipher.decryptValue(value);
        LOGGER.info("==== Decrypted Value ====\n[{}]", decrypted);

    }
}
