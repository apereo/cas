package org.apereo.cas.shell.commands.jasypt;

import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.registry.AlgorithmRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * This is {@link JasyptTestAlgorithmsCommand}.
 *
 * @author Hal Deadman
 * @since 5.3.0
 */
@ShellCommandGroup("Jasypt")
@Slf4j
@ShellComponent
public class JasyptTestAlgorithmsCommand {

    @Autowired
    private Environment environment;

    /**
     * List algorithms you can use Jasypt.
     *
     * @param includeBC whether to include the BouncyCastle provider
     */
    @ShellMethod(key = "jasypt-test-algorithms", value = "Test encryption algorithms you can use with Jasypt to make sure encryption and decryption both work")
    public void validateAlgorithms(@ShellOption(value = {"includeBC"},
        help = "Include Bouncy Castle provider") final boolean includeBC) {
        final String[] providers;
        if (includeBC) {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            providers = new String[]{BouncyCastleProvider.PROVIDER_NAME, "SunJCE"};
        } else {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
            providers = new String[]{"SunJCE"};
        }

        LOGGER.info("==== JASYPT Password Based Encryption Algorithms ====\n");
        val password = "SecretKeyValue";
        val value = "ValueToEncrypt";

        val pbeAlgos = AlgorithmRegistry.getAllPBEAlgorithms();
        for (val provider : providers) {
            for (val algorithm : pbeAlgos) {
                val cipher = new CasConfigurationJasyptCipherExecutor(this.environment);
                cipher.setPassword(password);
                cipher.setKeyObtentionIterations("1");
                cipher.setAlgorithm(algorithm.toString());
                cipher.setProviderName(provider);
                try {
                    var encryptedValue = StringUtils.EMPTY;
                    try {
                        encryptedValue = cipher.encryptValuePropagateExceptions(value);
                    } catch (final EncryptionInitializationException e) {
                        LOGGER.trace(e.getMessage(), e);
                        continue;
                    }
                    LOGGER.info("Provider: [{}] Algorithm: [{}]", provider, algorithm);
                    try {
                        cipher.decryptValuePropagateExceptions(encryptedValue);
                        LOGGER.info("Encrypted Value: [{}] Decryption succeeded", encryptedValue);
                    } catch (final Exception e) {
                        LOGGER.info("Encrypted Value: [{}] Decryption Failed", encryptedValue);
                    }
                } catch (final EncryptionInitializationException e) {
                    if (e.getCause() instanceof NoSuchAlgorithmException) {
                        LOGGER.info("Provider: [{}] does not support Algorithm: [{}]", provider, algorithm);
                    } else {
                        LOGGER.info("Error encrypting using provider: [{}] and algorithm: [{}], Message: {}", provider, algorithm, e.getMessage());
                    }
                }
            }
        }
    }
}
