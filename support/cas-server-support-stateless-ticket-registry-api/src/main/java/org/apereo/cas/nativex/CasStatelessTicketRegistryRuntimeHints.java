package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.registry.compact.TicketCompactor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasStatelessTicketRegistryRuntimeHints}.
 * Registers the ticket compactors found in CAS modules, such as those for OAuth tokens and simple multifactor
 * authentication tokens, along with the compactor proxies.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasStatelessTicketRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        val ticketCompactors = findSubclassesInPackage(TicketCompactor.class, CentralAuthenticationService.NAMESPACE);
        registerReflectionHints(hints, ticketCompactors);
        registerProxyHints(hints, TicketCompactor.class);
    }
}
