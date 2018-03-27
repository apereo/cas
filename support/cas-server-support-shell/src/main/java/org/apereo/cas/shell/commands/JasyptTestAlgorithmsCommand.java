package org.apereo.cas.shell.commands;

import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.registry.AlgorithmRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

/**
 * This is {@link JasyptTestAlgorithmsCommand}.
 *
 * @author Hal Deadman
 * @since 5.3.0
 */
@Slf4j
@Service
public class JasyptTestAlgorithmsCommand implements CommandMarker {

    @Autowired
    private Environment environment;
    
    /**
     * List algorithms you can use Jasypt.
     * @param includeBC      whether to include the BouncyCastle provider
     */
    @CliCommand(value = "jasypt-test-algorithms", help = "Test encryption alogrithms you can use with Jasypt to make sure encryption and decryption both work")
    public void testAlgorithms(@CliOption(key = { "includeBC" },
                                mandatory = false,
                                help = "Include Bouncy Castle provider",
                                specifiedDefaultValue = "true",
                                unspecifiedDefaultValue = "false") final boolean includeBC) {
        final String[] providers;
        if (includeBC) {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            providers = new String[] {BouncyCastleProvider.PROVIDER_NAME, "SunJCE"};
        } else {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
            providers = new String[] {"SunJCE"};
        }

        LOGGER.info("==== JASYPT Password Based Encryption Algorithms ====\n");
        final String password = "SecretKeyValue";
        final String value = "ValueToEncrypt";
                
        final Set<String> pbeAlgos = AlgorithmRegistry.getAllPBEAlgorithms();
        for (final String provider : providers) {
            for (final String algorithm : pbeAlgos) {
                final CasConfigurationJasyptCipherExecutor cipher = new CasConfigurationJasyptCipherExecutor(this.environment);
                cipher.setPassword(password);
                cipher.setKeyObtentionIterations("1");
                cipher.setAlgorithm(algorithm);
                cipher.setProviderName(provider);
                try {
                    final String encryptedValue;
                    try {
                        encryptedValue = cipher.encryptValuePropagateExceptions(value);
                    } catch (final EncryptionInitializationException e) {
                        // encryption doesn't work for this algorithm/provider combo
                        continue;
                    }
                    LOGGER.info("Provider: [{}] Algorithm: [{}]", provider, algorithm);
                    try {
                        cipher.decryptValuePropagateExceptions(encryptedValue);
                        LOGGER.info("Encrypted Value: [{}] Decryption Succeeded", encryptedValue);
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
