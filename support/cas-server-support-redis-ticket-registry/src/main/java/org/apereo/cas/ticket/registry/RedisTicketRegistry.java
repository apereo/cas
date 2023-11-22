package org.apereo.cas.ticket.registry;

import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The ticket registry is customized to remove the userid from
 * the redis cache key. It also removes the use of the KEYS command
 * to fetch a key by pattern before fetching it, and instead uses the
 * {@code CAS_TICKET:TICKET_ID} key construct to directly fetch the key.
 * The above optimization does come with a cost:
 * Note that as a result of this change, ticket registry operations
 * that need to query items by user will need to fetch all relevant tickets,
 * filter by type and user id to then find the final result set.
 *
 * @author serv
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class RedisTicketRegistry extends AbstractTicketRegistry {
    private static final String CAS_TICKET_PREFIX = "CAS_TICKET:";

    private static final String CAS_PRINCIPAL_PREFIX = "CAS_PRINCIPAL:";

    private final CasRedisTemplate<String, Ticket> client;

    private static Long getTimeout(final Ticket ticket) {
        val ttl = ticket.getExpirationPolicy().getTimeToLive();
        if (ttl > Integer.MAX_VALUE) {
            return (long) Integer.MAX_VALUE;
        } else if (ttl <= 0) {
            return 1L;
        }
        return ttl;
    }

    private static String getTicketRedisKey(final String ticketId) {
        return CAS_TICKET_PREFIX + StringUtils.defaultIfBlank(ticketId.trim(), "*");
    }

    private static String getPrincipalRedisKey(final String user) {
        return CAS_PRINCIPAL_PREFIX + user;
    }

    @Override
    @SuppressWarnings("java:S2583")
    public long deleteAll() {
        stream().forEach(ticket -> deleteSingleTicket(ticket.getId()));
        return 1;
    }

    @Override
    public long deleteSingleTicket(final String ticketId) {
        val redisKey = getTicketRedisKey(encodeTicketId(ticketId));
        val encodeTicket = getSingleTicketFromRedisKey(redisKey)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if(encodeTicket == null){
            LOGGER.trace("Ticket [{}] not found from redis.", ticketId);
            return 0;
        }
        val ticket = decodeInternal(encodeTicket);

        if (ticket instanceof TicketGrantingTicket) {
            val userId = getPrincipalIdFrom(ticket);
            if (StringUtils.isNotBlank(userId)) {
                val redisPrincipalPattern = getPrincipalRedisKey(encodeTicketId(userId));
                val ops = client.boundSetOps(redisPrincipalPattern);

                if(encodeTicket != null && Boolean.TRUE.equals(ops.isMember(encodeTicket))) {
                    ops.remove(encodeTicket);
                    LOGGER.debug("Remove ticket from principal set.");
                }else {
                    LOGGER.debug("No ticket found from principal set. ticket is{}, principal is {}", ticket.getId(), userId);
                }

            }
        }
        return Optional.of(redisKey).stream().mapToInt(id -> BooleanUtils.toBoolean(client.delete(id)) ? 1 : 0).sum();
    }

    @Override
    public void addTicketInternal(final Ticket ticket) {
        try {
            LOGGER.debug("Adding ticket [{}]", ticket);
            val userId = getPrincipalIdFrom(ticket);
            val encodedTicketId = encodeTicketId(ticket.getId());
            val redisKey = getTicketRedisKey(encodedTicketId);
            val encodeTicket = encodeTicket(ticket);
            val timeout = getTimeout(ticket);
            this.client.boundValueOps(redisKey).set(encodeTicket, timeout, TimeUnit.SECONDS);

            if (StringUtils.isNotBlank(userId) && ticket instanceof TicketGrantingTicket) {
                val redisPrincipalPattern = getPrincipalRedisKey(encodeTicketId(userId));
                val ops = client.boundSetOps(redisPrincipalPattern);
                ops.add(encodeTicket);
                ops.expire(timeout, TimeUnit.SECONDS);
            }

        } catch (final Exception e) {
            LOGGER.error("Failed to add [{}]", ticket);
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        try {
            val redisKey = getTicketRedisKey(encodeTicketId(ticketId));
            return getSingleTicketFromRedisKey(redisKey)
                .map(this::decodeTicket)
                .filter(predicate)
                .findFirst()
                .orElse(null);
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}]", ticketId);
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    private Stream<Ticket> getSingleTicketFromRedisKey(final String redisKey) {
        return Optional.of(redisKey)
            .stream()
            .map(key -> client.boundValueOps(key).get())
            .filter(Objects::nonNull);
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        try (val ticketsStream = stream()) {
            return ticketsStream.collect(Collectors.toSet());
        }
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        try {
            LOGGER.debug("Updating ticket [{}]", ticket);
            val encodeTicket = encodeTicket(ticket);

            val redisKey = getTicketRedisKey(encodeTicketId(ticket.getId()));
            LOGGER.debug("Fetched redis key [{}] for ticket [{}]", redisKey, ticket);

            val timeout = getTimeout(ticket);
            client.boundValueOps(redisKey).set(encodeTicket, timeout, TimeUnit.SECONDS);
            return encodeTicket;
        } catch (final Exception e) {
            LOGGER.error("Failed to update [{}]", ticket);
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public Stream<? extends Ticket> stream() {
        return getKeysStream()
            .map(redisKey -> {
                val ticket = this.client.boundValueOps(redisKey).get();
                if (ticket == null) {
                    this.client.delete(redisKey);
                    return null;
                }
                return ticket;
            })
            .filter(Objects::nonNull)
            .map(this::decodeTicket)
            .filter(Objects::nonNull);
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val redisKey = getPrincipalRedisKey(encodeTicketId(principalId));
        return client.boundSetOps(redisKey)
            .members()
            .stream()
            .filter(Objects::nonNull)
            .map(this::decodeTicket)
            .filter(Objects::nonNull);
    }

    private static String getPatternTicketRedisKey() {
        return CAS_TICKET_PREFIX + '*';
    }

    private Stream<String> getKeysStream() {
        return getKeysStream(getPatternTicketRedisKey());
    }

    private Stream<String> getKeysStream(final String key) {
        return Objects.requireNonNull(client.keys(key)).stream();
    }

}
