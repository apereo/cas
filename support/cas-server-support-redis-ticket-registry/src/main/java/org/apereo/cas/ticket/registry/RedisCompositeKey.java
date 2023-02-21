package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.val;

/**
 * This is {@link RedisCompositeKey}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
public class RedisCompositeKey {
    /**
     * Redis message topic key used to sync memory cache across nodes.
     */
    public static final String REDIS_TICKET_REGISTRY_MESSAGE_TOPIC = "redisTicketRegistryMessageTopic";

    /**
     * Ticket prefix.
     */
    public static final String CAS_TICKET_PREFIX = "CAS_TICKET";

    @Builder.Default
    private final String principal = "*";

    @Builder.Default
    private final String id = "*";

    @Builder.Default
    private final String prefix = "*";

    /**
     * To key pattern string.
     *
     * @return the string
     */
    public String toKeyPattern() {
        return String.format("%s:%s:%s:%s", CAS_TICKET_PREFIX, id, principal, prefix);
    }

    static String getPatternTicketRedisKey() {
        return CAS_TICKET_PREFIX + ":*";
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
}
