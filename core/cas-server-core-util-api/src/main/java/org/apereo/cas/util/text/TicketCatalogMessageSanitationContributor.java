package org.apereo.cas.util.text;

import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link TicketCatalogMessageSanitationContributor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class TicketCatalogMessageSanitationContributor implements MessageSanitationContributor {
    private final ObjectProvider<TicketCatalog> ticketCatalog;

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
