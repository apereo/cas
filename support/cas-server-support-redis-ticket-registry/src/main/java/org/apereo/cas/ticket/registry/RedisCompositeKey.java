package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link RedisCompositeKey}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
@AllArgsConstructor
@With
public class RedisCompositeKey {
    /**
     * Redis message topic key used to sync memory cache across nodes.
     */
    public static final String REDIS_TICKET_REGISTRY_MESSAGE_TOPIC = "redisTicketRegistryMessageTopic";

    /**
     * Ticket prefix.
     */
    static final String CAS_TICKET_PREFIX = "CAS_TICKET";

    /**
     * Principal prefix.
     */
    private static final String CAS_PRINCIPAL_PREFIX = "CAS_PRINCIPAL";

    @Builder.Default
    private final String query = "*";

    @Builder.Default
    private final String prefix = CAS_TICKET_PREFIX;

    /**
     * For tickets redis composite key.
     *
     * @return the redis composite key
     */
    public static RedisCompositeKey forTickets() {
        return RedisCompositeKey.builder().prefix(CAS_TICKET_PREFIX).build();
    }

    /**
     * For principal redis composite key.
     *
     * @return the redis composite key
     */
    public static RedisCompositeKey forPrincipal() {
        return RedisCompositeKey.builder().prefix(CAS_PRINCIPAL_PREFIX).build();
    }

    /**
     * To key pattern string.
     *
     * @return the string
     */
    public String toKeyPattern() {
        return String.format("%s:%s", prefix, query);
    }

    /**
     * If no time out value is specified, expire the ticket immediately.
     *
     * @param ticket the ticket
     * @return timeout
     */
    static Long getTimeout(final Ticket ticket) {
        val ttl = ticket.getExpirationPolicy().getTimeToLive();
        if (ttl > Integer.MAX_VALUE) {
            return (long) Integer.MAX_VALUE;
        }
        return ttl <= 0 ? 1L : ttl;
    }

    /**
     * Construct a redis key that can use pattern matching on IDs.
     *
     * @param id the id
     * @return the redis composite key
     */
    public RedisCompositeKey withIdPattern(final String id) {
        return new RedisCompositeKey(StringUtils.defaultString(id) + '*', prefix);
    }

    /**
     * With ticket id redis composite key.
     *
     * @param ticketPrefix the ticket prefix
     * @param encodedId    the encoded id
     * @return the redis composite key
     */
    public RedisCompositeKey withTicketId(final String ticketPrefix, final String encodedId) {
        return RedisCompositeKey.forTickets().withQuery(ticketPrefix + ':' + encodedId);
    }

    /**
     * Remove the starting prefix from the key.
     *
     * @param key the key
     * @return the string
     */
    public String withoutPrefix(final String key) {
        return StringUtils.removeStart(key, this.prefix + ':');
    }
}
