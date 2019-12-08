package org.apereo.cas.shell.commands.jasypt;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.registry.AlgorithmRegistry;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.security.Security;

/**
 * This is {@link JasyptListAlgorithmsCommand}.
 *
 * @author Hal Deadman
 * @since 5.3.0
 */
@ShellCommandGroup("Jasypt")
@Slf4j
@ShellComponent
public class JasyptListAlgorithmsCommand {

    /**
     * List algorithms you can use Jasypt.
     *
     * @param includeBC whether to include the BouncyCastle provider
     */
    @ShellMethod(key = "jasypt-list-algorithms", value = "List alogrithms you can use with Jasypt for property encryption")
    public void listAlgorithms(@ShellOption(value = { "includeBC", "--includeBC" },
        help = "Include Bouncy Castle provider") final boolean includeBC) {
        if (includeBC) {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
        } else {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
        val providers = Security.getProviders();
        LOGGER.info("Loaded providers: ");
        for (val provider : providers) {
            LOGGER.info("Provider: [{}] [{}]", provider.getName(), provider.getClass().getName());
        }
        val pbeAlgos = AlgorithmRegistry.getAllPBEAlgorithms();
        LOGGER.info("==== JASYPT Password Based Encryption Algorithms ====\n");
        for (val pbeAlgo : pbeAlgos) {
            LOGGER.info(pbeAlgo.toString());
        }
    }
}
