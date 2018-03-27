package org.apereo.cas.ticket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.OrderComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

/**
 * This is {@link DefaultTicketCatalog}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@NoArgsConstructor
public class DefaultTicketCatalog implements TicketCatalog {

    private final Map<String, TicketDefinition> ticketMetadataMap = new HashMap<>();

    @Override
    public TicketDefinition find(final String ticketId) {
        final TicketDefinition defn = ticketMetadataMap.values().stream()
            .filter(md -> ticketId.startsWith(md.getPrefix())).findFirst().orElse(null);
        if (defn == null) {
            LOGGER.error("Ticket definition for [{}] cannot be found in the ticket catalog "
                + "which only contains the following ticket types: [{}]", ticketId, ticketMetadataMap.keySet());
        }
        return defn;
    }

    @Override
    public TicketDefinition find(final Ticket ticket) {
        LOGGER.debug("Locating ticket definition for ticket [{}]", ticket);
        return find(ticket.getPrefix());
    }

    @Override
    public Collection<TicketDefinition> find(final Class<Ticket> ticketClass) {
        final List list = ticketMetadataMap.values().stream().filter(t -> t.getImplementationClass().isAssignableFrom(ticketClass)).collect(Collectors.toList());
        OrderComparator.sort(list);
        LOGGER.debug("Located all registered and known sorted ticket definitions [{}] that match [{}]", list, ticketClass);
        return list;
    }

    @Override
    public void register(final TicketDefinition ticketDefinition) {
        LOGGER.debug("Registering/Updating ticket definition [{}]", ticketDefinition);
        ticketMetadataMap.put(ticketDefinition.getPrefix(), ticketDefinition);
    }

    @Override
    public void update(final TicketDefinition metadata) {
        register(metadata);
    }

    @Override
    public boolean contains(final String ticketId) {
        LOGGER.debug("Locating ticket definition for [{}]", ticketId);
        return ticketMetadataMap.containsKey(ticketId);
    }

    @Override
    public Collection<TicketDefinition> findAll() {
        final List list = new ArrayList<>(ticketMetadataMap.values());
        OrderComparator.sort(list);
        LOGGER.debug("Located all registered and known sorted ticket definitions [{}]", list);
        return list;
    }
}
