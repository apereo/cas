package org.apereo.cas.ticket.registry;

import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Key-value ticket registry implementation that stores tickets in redis keyed on the ticket ID.
 *
 * @author serv
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedisTicketRegistry extends AbstractTicketRegistry {
    private static final String CAS_TICKET_PREFIX = "CAS_TICKET";

    private final CasRedisTemplate<String, Ticket> redisTemplate;

    /**
     * If not time out value is specified, expire the ticket immediately.
     *
     * @param ticket the ticket
     * @return timeout
     */
    private static Long getTimeout(final Ticket ticket) {
        val ttl = ticket.getExpirationPolicy().getTimeToLive();
        if (ttl > Integer.MAX_VALUE) {
            return (long) Integer.MAX_VALUE;
        } else if (ttl <= 0) {
            return 1L;
        }
        return ttl;
    }

    private static String getPatternTicketRedisKey() {
        return CAS_TICKET_PREFIX + ":*";
    }

    @Override
    public long deleteAll() {
        val redisKeys = scanKeys().collect(Collectors.toSet());
        val size = Objects.requireNonNull(redisKeys).size();
        this.redisTemplate.delete(redisKeys);
        return size;
    }

    @Override
    public long deleteSingleTicket(final String ticketId) {
        val redisKey = RedisCompositeKey.builder().id(encodeTicketId(ticketId)).build().toKeyPattern();
        return scanKeys(redisKey).mapToInt(id -> BooleanUtils.toBoolean(redisTemplate.delete(id)) ? 1 : 0).sum();
    }

    @Override
    public void addTicket(final Stream<? extends Ticket> toSave) {
        FunctionUtils.doAndHandle(__ ->
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                toSave.forEach(this::addTicketInternal);
                return null;
            }));
    }

    @Override
    public void addTicketInternal(final Ticket ticket) {
        FunctionUtils.doAndHandle(__ -> {
            LOGGER.debug("Adding ticket [{}]", ticket);
            val userId = getPrincipalIdFrom(ticket);
            val redisKey = RedisCompositeKey.builder()
                .id(encodeTicketId(ticket.getId()))
                .principal(encodeTicketId(userId))
                .prefix(ticket.getPrefix())
                .build()
                .toKeyPattern();
            val encodeTicket = encodeTicket(ticket);
            val timeout = getTimeout(ticket);
            redisTemplate.boundValueOps(redisKey).set(encodeTicket, timeout, TimeUnit.SECONDS);
        });
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        return FunctionUtils.doAndHandle(() -> {
            val prefix = StringUtils.substring(ticketId, 0, ticketId.indexOf(UniqueTicketIdGenerator.SEPARATOR));
            val redisKey = RedisCompositeKey.builder()
                .id(encodeTicketId(ticketId))
                .prefix(prefix)
                .build().toKeyPattern();
            return scanKeys(redisKey)
                .map(key -> redisTemplate.boundValueOps(key).get())
                .filter(Objects::nonNull)
                .map(this::decodeTicket)
                .filter(predicate)
                .findFirst()
                .orElse(null);
        });
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        try (val ticketsStream = stream()) {
            return ticketsStream.collect(Collectors.toSet());
        }
    }

    @Override
    public Stream<? extends Ticket> stream() {
        return scanKeys()
            .map(redisKey -> {
                val ticket = redisTemplate.boundValueOps(redisKey).get();
                if (ticket == null) {
                    redisTemplate.delete(redisKey);
                    return null;
                }
                return ticket;
            })
            .filter(Objects::nonNull)
            .map(this::decodeTicket)
            .filter(Objects::nonNull);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        try {
            LOGGER.debug("Updating ticket [{}]", ticket);
            val encodeTicket = encodeTicket(ticket);
            val userId = getPrincipalIdFrom(ticket);
            val redisKey = RedisCompositeKey.builder()
                .id(encodeTicketId(ticket.getId()))
                .principal(encodeTicketId(userId))
                .prefix(ticket.getPrefix())
                .build()
                .toKeyPattern();
            LOGGER.debug("Fetched redis key [{}] for ticket [{}]", redisKey, ticket);
            val timeout = getTimeout(ticket);
            redisTemplate.boundValueOps(redisKey).set(encodeTicket, timeout, TimeUnit.SECONDS);
            return encodeTicket;
        } catch (final Exception e) {
            LOGGER.error("Failed to update [{}]", ticket);
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val redisKey = RedisCompositeKey.builder()
            .principal(encodeTicketId(principalId))
            .prefix(TicketGrantingTicket.PREFIX)
            .build()
            .toKeyPattern();
        return scanKeys(redisKey)
            .map(key -> redisTemplate.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .map(this::decodeTicket)
            .filter(Objects::nonNull);
    }

    /**
     * Get a stream of all CAS-related keys from Redis DB.
     *
     * @return stream of all CAS-related keys from Redis DB
     */
    private Stream<String> scanKeys() {
        return scanKeys(getPatternTicketRedisKey());
    }

    private Stream<String> scanKeys(final String key) {
        LOGGER.debug("Loading keys for pattern [{}]", key);
        return Objects.requireNonNull(redisTemplate.keys(key)).stream();
    }

    @Override
    public long sessionCount() {
        val redisKey = RedisCompositeKey.builder()
            .prefix(TicketGrantingTicket.PREFIX)
            .build()
            .toKeyPattern();
        val keys = scanKeys(redisKey).collect(Collectors.toList());
        return Objects.requireNonNull(redisTemplate.countExistingKeys(keys));
    }

    @Override
    public long serviceTicketCount() {
        val redisKey = RedisCompositeKey.builder()
            .prefix(ServiceTicket.PREFIX)
            .build()
            .toKeyPattern();
        val keys = scanKeys(redisKey).collect(Collectors.toList());
        return Objects.requireNonNull(redisTemplate.countExistingKeys(keys));
    }

    @SuperBuilder
    @Getter
    private static class RedisCompositeKey {
        @Builder.Default
        private final String principal = "*";

        @Builder.Default
        private final String id = "*";

        @Builder.Default
        private final String prefix = "*";

        public String toKeyPattern() {
            return String.format("%s:%s:%s:%s", CAS_TICKET_PREFIX, id, principal, prefix);
        }
    }

}
