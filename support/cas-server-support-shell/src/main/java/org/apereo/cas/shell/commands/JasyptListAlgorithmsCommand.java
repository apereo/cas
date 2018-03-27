package org.apereo.cas.shell.commands;

import java.security.Provider;
import java.security.Security;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.registry.AlgorithmRegistry;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

/**
 * This is {@link JasyptListAlgorithmsCommand}.
 *
 * @author Hal Deadman
 * @since 5.3.0
 */
@Slf4j
@Service
public class JasyptListAlgorithmsCommand implements CommandMarker {

    /**
     * List algorithms you can use Jasypt.
     * @param includeBC      whether to include the BouncyCastle provider
     */
    @CliCommand(value = "jasypt-list-algorithms", help = "List alogrithms you can use with Jasypt for property encryption")
    public void listAlgorithms(@CliOption(key = { "includeBC" }, 
                                mandatory = false, 
                                help = "Include Bouncy Castle provider",  
                                specifiedDefaultValue = "true",
                                unspecifiedDefaultValue = "false") final boolean includeBC) {
        if (includeBC) {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
        } else {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
        final Provider[] providers = Security.getProviders();
        LOGGER.info("Loaded providers: ");
        for (final Provider provider : providers) {
            LOGGER.info("Provider: [{}] [{}]", provider.getName(), provider.getClass().getName());
        }
        final Set<String> pbeAlgos = AlgorithmRegistry.getAllPBEAlgorithms();
        LOGGER.info("==== JASYPT Password Based Encryption Algorithms ====\n");
        for (final String pbeAlgo : pbeAlgos) {
            LOGGER.info(pbeAlgo);
        }
    }
}
