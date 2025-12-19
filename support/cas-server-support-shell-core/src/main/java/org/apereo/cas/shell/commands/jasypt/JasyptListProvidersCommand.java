package org.apereo.cas.shell.commands.jasypt;

import module java.base;
import org.apereo.cas.shell.commands.CasShellCommand;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

/**
 * This is {@link JasyptListProvidersCommand}.
 *
 * @author Hal Deadman
 * @since 5.3.0
 */
@Slf4j
public class JasyptListProvidersCommand implements CasShellCommand {

    /**
     * List providers you can use Jasypt.
     *
     * @param includeBC whether to include the BouncyCastle provider
     */
    @Command(group = "Jasypt", name = "jasypt-list-providers", description = "List encryption providers with PBE Ciphers you can use with Jasypt")
    public void listProviders(
        @Option(
            longName = "includeBC", description = "Include Bouncy Castle provider", defaultValue = "false")
        final Boolean includeBC) {
        if (includeBC) {
            Security.addProvider(new BouncyCastleProvider());
        } else {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }

        val providers = Security.getProviders();
        for (val provider : providers) {
            val services = provider.getServices();
            val algorithms = services.stream()
                .filter(service -> "Cipher".equals(service.getType()) && service.getAlgorithm().contains("PBE"))
                .map(Provider.Service::getAlgorithm).toList();
            if (!algorithms.isEmpty()) {
                LOGGER.info("Provider: Name: [{}] Class: [{}]", provider.getName(), provider.getClass().getName());
                for (val algorithm : algorithms) {
                    LOGGER.info(" - Algorithm: [{}]", algorithm);
                }
            }
        }
    }
}
