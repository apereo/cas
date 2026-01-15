package org.apereo.cas.shell.commands.jasypt;

import module java.base;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.shell.commands.CasShellCommand;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.registry.AlgorithmRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.shell.core.command.annotation.Command;

/**
 * This is {@link JasyptTestAlgorithmsCommand}.
 *
 * @author Hal Deadman
 * @since 5.3.0
 */
@Slf4j
public class JasyptTestAlgorithmsCommand implements CasShellCommand {

    @Autowired
    private Environment environment;

    /**
     * List algorithms you can use Jasypt.
     */
    @Command(group = "Jasypt", name = "jasypt-test-algorithms",
        description = "Test encryption algorithms you can use with Jasypt to make sure encryption and decryption both work")
    public void validateAlgorithms() {
        val providers = new String[]{BouncyCastleProvider.PROVIDER_NAME, "SunJCE"};
        LOGGER.info("==== JASYPT Password Based Encryption Algorithms ====\n");
        val password = "SecretKeyValue";
        val value = "ValueToEncrypt";

        val pbeAlgos = AlgorithmRegistry.getAllPBEAlgorithms();
        for (val provider : providers) {
            LOGGER.trace("Testing provider [{}]", provider);
            for (val algorithm : pbeAlgos) {
                val cipher = new CasConfigurationJasyptCipherExecutor(this.environment);
                val algorithmStr = algorithm.toString();
                cipher.setPassword(password);
                cipher.setKeyObtentionIterations("1");
                cipher.setProviderName(provider);
                try {
                    LOGGER.trace("Testing algorithm [{}]", algorithmStr);
                    cipher.setAlgorithm(algorithmStr);
                    val encryptedValue = cipher.encryptValue(value, e -> {
                        LOGGER.trace(e.getMessage(), e);
                        return null;
                    });
                    if (encryptedValue == null) {
                        continue;
                    }
                    LOGGER.info("Provider: [{}] Algorithm: [{}]", provider, algorithmStr);
                    val result = cipher.decryptValue(encryptedValue);
                    FunctionUtils.doIf(result != null,
                            r -> LOGGER.info("Encrypted Value: [{}] Decryption succeeded", encryptedValue),
                            t -> LOGGER.warn("Encrypted Value: [{}] Decryption Failed", encryptedValue))
                        .accept(result);

                } catch (final Exception e) {
                    if (e.getCause() instanceof NoSuchAlgorithmException) {
                        LOGGER.warn("Provider: [{}] does not support Algorithm: [{}]", provider, algorithmStr);
                    } else {
                        LOGGER.warn("Error encrypting using provider: [{}] and algorithm: [{}], Message: [{}]", provider, algorithmStr, e.getMessage());
                    }
                }
            }
        }
    }
}
