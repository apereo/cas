package org.apereo.cas.shell.commands.jasypt;

import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.security.Security;

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
     * @param alg        the alg
     * @param provider   the provider
     * @param password   the password
     * @param iterations the iterations - defaults to {@value StandardPBEByteEncryptor#DEFAULT_KEY_OBTENTION_ITERATIONS}
     */
    @ShellMethod(key = "encrypt-value", value = "Encrypt a CAS property value/setting via Jasypt")
    public void encryptValue(
        @ShellOption(value = {"value"},
            help = "Value to encrypt") final String value,
        @ShellOption(value = {"alg"},
            help = "Algorithm to use to encrypt") final String alg,
        @ShellOption(value = {"provider"},
            help = "Security provider to use to encrypt") final String provider,
        @ShellOption(value = {"password"},
            help = "Password (encryption key) to encrypt") final String password,
        @ShellOption(value = {"iterations"},
            defaultValue = ShellOption.NULL,
            help = "Key obtention iterations to encrypt, default 1000") final String iterations) {

        val cipher = new CasConfigurationJasyptCipherExecutor(this.environment);
        cipher.setAlgorithm(alg);
        cipher.setPassword(password);
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        cipher.setProviderName(provider);
        cipher.setKeyObtentionIterations(iterations);
        val encrypted = cipher.encryptValue(value);
        LOGGER.info("==== Encrypted Value ====\n{}", encrypted);
    }
}
