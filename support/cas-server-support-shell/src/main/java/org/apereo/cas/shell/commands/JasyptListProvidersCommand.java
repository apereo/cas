package org.apereo.cas.shell.commands;

import java.security.Provider;
import java.security.Security;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

/**
 * This is {@link JasyptListProvidersCommand}.
 *
 * @author Hal Deadman
 * @since 5.3.0
 */
@Slf4j
@Service
public class JasyptListProvidersCommand implements CommandMarker {

    /**
     * List providers you can use Jasypt.
     * @param includeBC      whether to include the BouncyCastle provider
     */
    @CliCommand(value = "jasypt-list-providers", help = "List encryption providers with PBE Ciphers you can use with Jasypt")
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
        for (final Provider provider : providers) {
            final Set<Provider.Service> services = provider.getServices();
            final List<String> algorithms = 
                services.stream()
                        .filter(service -> "Cipher".equals(service.getType()) && service.getAlgorithm().contains("PBE"))
                        .map(service -> service.getAlgorithm())
                        .collect(Collectors.toList());
            if (!algorithms.isEmpty()) {
                LOGGER.info("Provider: Name: [{}] Class: [{}]", provider.getName(), provider.getClass().getName());
                for (final String algorithm : algorithms) {
                    LOGGER.info(" - Algorithm: [{}]", algorithm);                    
                }
            }
        }
    }
}
