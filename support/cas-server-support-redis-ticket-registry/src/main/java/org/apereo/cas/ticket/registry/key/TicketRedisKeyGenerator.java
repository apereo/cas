package org.apereo.cas.ticket.registry.key;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.RedisCompositeKey;

/**
 * This is {@link TicketRedisKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class TicketRedisKeyGenerator implements RedisKeyGenerator {

    @Override
    public String getType() {
        return Ticket.class.getName();
    }

    @Override
    public String forAllEntries() {
        return RedisCompositeKey.forTickets().toKeyPattern();
    }

    @Override
    public String forEntry(final String type, final String entry) {
        return RedisCompositeKey.forTickets().withTicketId(type, entry).toKeyPattern();
    }

    @Override
    public String forEntryType(final String type) {
        return RedisCompositeKey.forTickets().withIdPattern(type).toKeyPattern();
    }

    @Override
    public String rawKey(final String type) {
        return RedisCompositeKey.forTickets().withoutPrefix(type);
    }

    @Override
    public String getNamespace() {
        return RedisCompositeKey.forTickets().getPrefix();
    }
}
