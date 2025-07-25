package org.apereo.cas.ticket.registry.key;

import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;

/**
 * This is {@link DefaultRedisKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Getter
public class DefaultRedisKeyGenerator implements RedisKeyGenerator {

    private final TicketCatalog ticketCatalog;
    private final String namespace;
    private final String prefix;

    @Override
    public String forId(final String id) {
        return RedisCompositeKey.builder()
            .namespace(this.namespace)
            .id(id)
            .build()
            .toString();
    }

    @Override
    public String forPrefixAndId(final String prefix, final String ticketId) {
        return RedisCompositeKey.builder()
            .namespace(this.namespace)
            .prefix(prefix)
            .id(ticketId)
            .build()
            .toString();
    }

    @Override
    public String rawKey(final String type) {
        var withoutNamespace = Strings.CI.remove(type, this.namespace + ':');
        return ticketCatalog.findAll()
            .stream()
            .filter(ticketDefinition -> withoutNamespace.startsWith(ticketDefinition.getPrefix() + ':'))
            .findFirst()
            .map(ticketDefinition -> Strings.CI.removeStart(withoutNamespace, ticketDefinition.getPrefix() + ':'))
            .orElse(withoutNamespace);
    }

    @Override
    public String getKeyspace() {
        return RedisCompositeKey.builder().namespace(this.namespace).prefix(prefix).build().toString();
    }

    @Override
    public boolean isTicketKeyGenerator() {
        return REDIS_NAMESPACE_TICKETS.equalsIgnoreCase(this.namespace);
    }

    /**
     * For ticket redis key generator.
     *
     * @param ticketCatalog    the ticket catalog
     * @param ticketDefinition the ticket definition
     * @return the redis key generator
     */
    public static RedisKeyGenerator forTicket(final TicketCatalog ticketCatalog, final TicketDefinition ticketDefinition) {
        return new DefaultRedisKeyGenerator(ticketCatalog, REDIS_NAMESPACE_TICKETS, ticketDefinition.getPrefix());
    }

    /**
     * For principals redis key generator.
     *
     * @param ticketCatalog the ticket catalog
     * @param type          the type
     * @return the redis key generator
     */
    public static RedisKeyGenerator forPrincipal(final TicketCatalog ticketCatalog, final String type) {
        return new DefaultRedisKeyGenerator(ticketCatalog, REDIS_NAMESPACE_PRINCIPALS, type);
    }
}
