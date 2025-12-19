package org.apereo.cas.shell.commands.jasypt;

import module java.base;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.shell.commands.CasShellCommand;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.jasypt.iv.NoIvGenerator;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

/**
 * This is {@link JasyptEncryptPropertyCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JasyptEncryptPropertyCommand implements CasShellCommand {
    @Autowired
    private Environment environment;

    /**
     * Encrypt a value using Jasypt.
     *
     * @param value      the value
     * @param file       the file
     * @param alg        the alg
     * @param provider   the provider
     * @param password   the password
     * @param initVector the init vector
     * @param iterations the iterations - defaults to {@value StandardPBEByteEncryptor#DEFAULT_KEY_OBTENTION_ITERATIONS}
     * @throws Exception the exception
     */
    @Command(group = "CAS Properties", name = "encrypt-value", description = "Encrypt a CAS property value/setting via Jasypt")
    public void encryptValue(
        @Option(
            longName = "value",
            description = "Value to encrypt"
        )
        final String value,

        @Option(
            longName = "file",
            description = "File to encrypt"
        )
        final String file,

        @Option(
            longName = "alg",
            description = "Algorithm to use to encrypt"
        )
        final String alg,

        @Option(
            longName = "provider",
            description = "Security provider to use to encrypt"
        )
        final String provider,

        @Option(
            longName = "password",
            description = "Password (encryption key) to encrypt"
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
            description = "Key obtention iterations to encrypt, default 1000"
        )
        final String iterations
    ) throws Exception {

        val cipher = new CasConfigurationJasyptCipherExecutor(environment);
        cipher.setAlgorithm(alg);
        cipher.setPassword(password);
        cipher.setProviderName(provider);
        cipher.setKeyObtentionIterations(iterations);
        cipher.setIvGenerator(initVector ? new RandomIvGenerator() : new NoIvGenerator());

        val encrypted = doEncrypt(value, file, cipher);
        //CHECKSTYLE:OFF
        LOGGER.info("==== Encrypted Value ====\n{}", encrypted);
        //CHECKSTYLE:ON
    }

    private static String doEncrypt(final String value, final String file,
                                    final CasConfigurationJasyptCipherExecutor cipher) throws IOException {
        if (StringUtils.isNotBlank(file)) {
            val contents = FileUtils.readFileToString(new File(file), StandardCharsets.UTF_8);
            return cipher.encryptValue(contents);
        }
        return cipher.encryptValue(value);
    }
}
