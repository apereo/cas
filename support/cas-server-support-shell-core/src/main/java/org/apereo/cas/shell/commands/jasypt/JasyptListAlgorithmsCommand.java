package org.apereo.cas.shell.commands.jasypt;

import module java.base;
import org.apereo.cas.shell.commands.CasShellCommand;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.registry.AlgorithmRegistry;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

/**
 * This is {@link JasyptListAlgorithmsCommand}.
 *
 * @author Hal Deadman
 * @since 5.3.0
 */
@Slf4j
public class JasyptListAlgorithmsCommand implements CasShellCommand {

    /**
     * List algorithms you can use Jasypt.
     *
     * @param includeBC whether to include the BouncyCastle provider
     */
    @Command(group = "Jasypt", name = "jasypt-list-algorithms", description = "List algorithms you can use with Jasypt for property encryption")
    public void listAlgorithms(
        @Option(
            longName = "includeBC", description = "Include Bouncy Castle provider", defaultValue = "false")
        final Boolean includeBC) {
        if (includeBC) {
            Security.addProvider(new BouncyCastleProvider());
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
