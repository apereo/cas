package org.apereo.cas.shell.commands.jasypt;

import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.jasypt.iv.NoIvGenerator;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link JasyptEncryptPropertyCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ShellCommandGroup("CAS Properties")
@ShellComponent
@Slf4j
public class JasyptEncryptPropertyCommand {
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
    @ShellMethod(key = "encrypt-value", value = "Encrypt a CAS property value/setting via Jasypt")
    public void encryptValue(
        @ShellOption(value = {"value", "--value"},
            defaultValue = ShellOption.NULL,
            help = "Value to encrypt")
        final String value,
        @ShellOption(value = {"file", "--file"},
            defaultValue = ShellOption.NULL,
            help = "File to encrypt")
        final String file,
        @ShellOption(defaultValue = ShellOption.NULL,
            value = {"alg", "--alg"},
            help = "Algorithm to use to encrypt")
        final String alg,
        @ShellOption(defaultValue = ShellOption.NULL,
            value = {"provider", "--provider"},
            help = "Security provider to use to encrypt")
        final String provider,
        @ShellOption(value = {"password", "--password"},
            help = "Password (encryption key) to encrypt")
        final String password,
        @ShellOption(value = {"initvector", "--initvector", "iv", "--iv"},
            help = "Use initialization vector to encrypt", defaultValue = "false")
        final Boolean initVector,
        @ShellOption(value = {"iterations", "--iterations"},
            defaultValue = ShellOption.NULL,
            help = "Key obtention iterations to encrypt, default 1000")
        final String iterations) throws Exception {

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
