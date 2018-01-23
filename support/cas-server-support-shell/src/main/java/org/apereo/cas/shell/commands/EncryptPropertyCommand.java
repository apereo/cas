package org.apereo.cas.shell.commands;

import java.security.Security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

/**
 * This is {@link EncryptPropertyCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Service
@Slf4j
public class EncryptPropertyCommand implements CommandMarker {


    @Autowired
    private Environment environment;

    /**
     * Encrypt a value using Jasypt.
     *
     * @param value      the value
     * @param alg        the alg
     * @param provider   the provider
     * @param password   the password
     * @param iterations the iterations
     */
    @CliCommand(value = "encrypt-value", help = "Encrypt a CAS property value/setting via Jasypt")
    public void encryptValue(
        @CliOption(key = {"value"},
            help = "Value to encrypt",
            mandatory = true,
            optionContext = "Value to encrypt") final String value,
         @CliOption(key = {"alg"},
             help = "Algorithm to use to encrypt",
             optionContext = "Algorithm to use to encrypt",
             specifiedDefaultValue = StringUtils.EMPTY,
             unspecifiedDefaultValue = StringUtils.EMPTY) final String alg,
         @CliOption(key = {"provider"},
             help = "Security provider to use to encrypt",
             optionContext = "Security provider to use to encrypt (Enter BC for BouncyCastle)",
             specifiedDefaultValue = StringUtils.EMPTY,
             unspecifiedDefaultValue = StringUtils.EMPTY) final String provider,
         @CliOption(key = {"password"},
             mandatory = true,
             help = "Password (encryption key) to encrypt",
             optionContext = "Password (encryption key) to encrypt") final String password,
         @CliOption(key = {"iterations"},
             help = "Key obtention iterations to encrypt",
             optionContext = "Key obtention iterations to encrypt",
             specifiedDefaultValue = StringUtils.EMPTY,
             unspecifiedDefaultValue = StringUtils.EMPTY) final String iterations) {

        final CasConfigurationJasyptCipherExecutor cipher = new CasConfigurationJasyptCipherExecutor(this.environment);
        cipher.setAlgorithm(alg);
        cipher.setPassword(password);
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        cipher.setProviderName(provider);
        cipher.setKeyObtentionIterations(iterations);
        final String encrypted = cipher.encryptValue(value);
        LOGGER.info("==== Encrypted Value ====\n{}", encrypted);
        try {
            cipher.decryptValue(encrypted);
        } catch (final Exception e) {
            LOGGER.error("Decryption failed for value: {}", encrypted, e);
        }
    }
}
