package org.apereo.cas.shell.commands.jasypt;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.security.Provider;
import java.security.Security;
import java.util.stream.Collectors;

/**
 * This is {@link JasyptListProvidersCommand}.
 *
 * @author Hal Deadman
 * @since 5.3.0
 */
@ShellCommandGroup("Jasypt")
@Slf4j
@ShellComponent
public class JasyptListProvidersCommand {

    /**
     * List providers you can use Jasypt.
     *
     * @param includeBC whether to include the BouncyCastle provider
     */
    @ShellMethod(key = "jasypt-list-providers", value = "List encryption providers with PBE Ciphers you can use with Jasypt")
    public void listProviders(@ShellOption(value = { "includeBC", "--includeBC" },
        help = "Include Bouncy Castle provider") final boolean includeBC) {
        if (includeBC) {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
        } else {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }

        val providers = Security.getProviders();
        for (val provider : providers) {
            val services = provider.getServices();
            val algorithms =
                services.stream()
                    .filter(service -> "Cipher".equals(service.getType()) && service.getAlgorithm().contains("PBE"))
                    .map(Provider.Service::getAlgorithm)
                    .collect(Collectors.toList());
            if (!algorithms.isEmpty()) {
                LOGGER.info("Provider: Name: [{}] Class: [{}]", provider.getName(), provider.getClass().getName());
                for (val algorithm : algorithms) {
                    LOGGER.info(" - Algorithm: [{}]", algorithm);
                }
            }
        }
    }
}
