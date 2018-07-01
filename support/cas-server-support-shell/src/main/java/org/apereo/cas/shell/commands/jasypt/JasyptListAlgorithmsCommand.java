package org.apereo.cas.shell.commands.jasypt;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.registry.AlgorithmRegistry;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.security.Security;
import java.util.Set;

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
    public void listAlgorithms(@ShellOption(value = {"includeBC"},
        help = "Include Bouncy Castle provider") final boolean includeBC) {
        if (includeBC) {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
        } else {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
        final var providers = Security.getProviders();
        LOGGER.info("Loaded providers: ");
        for (final var provider: providers) {
            LOGGER.info("Provider: [{}] [{}]", provider.getName(), provider.getClass().getName());
        }
        final Set<String> pbeAlgos = AlgorithmRegistry.getAllPBEAlgorithms();
        LOGGER.info("==== JASYPT Password Based Encryption Algorithms ====\n");
        for (final var pbeAlgo: pbeAlgos) {
            LOGGER.info(pbeAlgo);
        }
    }
}
