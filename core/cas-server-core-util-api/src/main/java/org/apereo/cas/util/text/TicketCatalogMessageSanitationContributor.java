package org.apereo.cas.util.text;

import module java.base;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link TicketCatalogMessageSanitationContributor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class TicketCatalogMessageSanitationContributor implements MessageSanitationContributor {
    private final ObjectProvider<@NonNull TicketCatalog> ticketCatalog;

    @Override
    public List<String> getTicketIdentifierPrefixes() {
        return ticketCatalog
            .stream()
            .map(TicketCatalog::findAll)
            .flatMap(Collection::stream)
            .map(TicketDefinition::getPrefix)
            .collect(Collectors.toList());
    }
}
