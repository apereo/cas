package org.apereo.cas.ticket.registry.key;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * This is {@link RedisKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface RedisKeyGenerator {
    /**
     * The namespace for all CAS tickets.
     */
    String REDIS_NAMESPACE_TICKETS = "CAS_TICKET";
    /**
     * The namespace for all CAS principals.
     */
    String REDIS_NAMESPACE_PRINCIPALS = "CAS_PRINCIPAL";

    /**
     * Redis message topic key used to sync memory cache across nodes.
     */
    String REDIS_TICKET_REGISTRY_MESSAGE_TOPIC = "redisTicketRegistryMessageTopic";
    
    /**
     * Gets type.
     *
     * @return the type
     */
    String getPrefix();

    /**
     * For entry string.
     *
     * @param entry the entry
     * @return the string
     */
    String forId(String entry);

    /**
     * For entry string.
     *
     * @param type  the type
     * @param entry the entry
     * @return the string
     */
    String forPrefixAndId(String type, String entry);
    
    /**
     * Raw key string.
     *
     * @param type the type
     * @return the string
     */
    String rawKey(String type);

    /**
     * Gets namespace.
     *
     * @return the namespace
     */
    String getNamespace();

    /**
     * Gets ticket catalog.
     *
     * @return the ticket catalog
     */
    TicketCatalog getTicketCatalog();

    /**
     * Gets keyspace.
     *
     * @return the keyspace
     */
    String getKeyspace();

    /**
     * Is ticket key generator?.
     *
     * @return true or false
     */
    boolean isTicketKeyGenerator();

    /**
     * If no time out value is specified, expire the ticket immediately.
     *
     * @param ticket the ticket
     * @return timeout
     */
    static Long getTicketExpirationInSeconds(final Ticket ticket) {
        val ttl = ticket.getExpirationPolicy().getTimeToLive();
        if (ttl > Integer.MAX_VALUE) {
            return (long) Integer.MAX_VALUE;
        }
        return ttl <= 0 ? 1L : ttl;
    }

    /**
     * Parse key into redis composite key.
     *
     * @param key the redis key pattern
     * @return the redis composite key
     */
    static RedisCompositeKey parse(final String key) {
        val patternBits = Splitter.on(':').splitToList(key);
        if (patternBits.size() == 2) {
            Assert.isTrue(patternBits.getFirst().equals(REDIS_NAMESPACE_PRINCIPALS), "Unknown principal key pattern %s".formatted(key));
            return RedisCompositeKey.builder()
                .namespace(patternBits.getFirst())
                .id(patternBits.getLast())
                .build();
        }
        if (patternBits.size() == 3) {
            Assert.isTrue(patternBits.getFirst().equals(REDIS_NAMESPACE_TICKETS), "Unknown ticket key pattern %s".formatted(key));
            return RedisCompositeKey.builder()
                .namespace(patternBits.getFirst())
                .prefix(patternBits.get(1))
                .id(patternBits.getLast())
                .build();
        }
        throw new IllegalArgumentException("Unable to parse pattern " + key);
    }

    /**
     * For all entries and types.
     *
     * @return the string
     */
    default String forEverything() {
        return forPrefixAndId("*", "*");
    }
    
    @SuperBuilder
    @Getter
    class RedisCompositeKey {
        private final String namespace;

        private final String prefix;

        private final String id;

        @Override
        public String toString() {
            var pattern = StringUtils.EMPTY;
            if (StringUtils.isNotBlank(namespace)) {
                pattern += namespace;
            }
            if (StringUtils.isNotBlank(prefix)) {
                pattern += ':' + prefix;
            }
            if (StringUtils.isNotBlank(id)) {
                pattern += ':' + id;
            }
            return pattern;
        }
    }
}
