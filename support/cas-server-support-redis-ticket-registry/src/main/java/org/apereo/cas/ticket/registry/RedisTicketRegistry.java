package org.apereo.cas.ticket.registry;

import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.pub.RedisTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.redis.lettucemod.api.sync.RediSearchCommands;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Field;
import io.lettuce.core.RedisCommandExecutionException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.springframework.data.redis.core.RedisCallback;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
public class RedisTicketRegistry extends AbstractTicketRegistry {

    private static final String SEARCH_INDEX_NAME = RedisTicketDocument.class.getSimpleName() + "Index";

    private final CasRedisTemplate<String, RedisTicketDocument> redisTemplate;

    private final Cache<String, Ticket> ticketCache;

    private final RedisTicketRegistryMessagePublisher messagePublisher;

    private final Optional<RediSearchCommands> searchCommands;

    public RedisTicketRegistry(final CipherExecutor cipherExecutor,
                               final TicketSerializationManager ticketSerializationManager,
                               final TicketCatalog ticketCatalog,
                               final CasRedisTemplate<String, RedisTicketDocument> redisTemplate,
                               final Cache<String, Ticket> ticketCache,
                               final RedisTicketRegistryMessagePublisher messagePublisher,
                               final Optional<RediSearchCommands> searchCommands) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog);
        this.redisTemplate = redisTemplate;
        this.ticketCache = ticketCache;
        this.messagePublisher = messagePublisher;
        this.searchCommands = searchCommands;

        createIndexesIfNecessary();
    }

    @Override
    public long deleteAll() {
        val redisKeys = scanKeys().collect(Collectors.toSet());
        val size = Objects.requireNonNull(redisKeys).size();
        redisTemplate.delete(redisKeys);
        ticketCache.invalidateAll();
        messagePublisher.deleteAll();
        return size;
    }

    @Override
    public long deleteSingleTicket(final String ticketId) {
        val redisKey = RedisCompositeKey.builder().id(digest(ticketId)).build();
        val redisKeyPattern = redisKey.toKeyPattern();
        val count = scanKeys(redisKeyPattern)
            .mapToInt(id -> BooleanUtils.toBoolean(redisTemplate.delete(id)) ? 1 : 0)
            .sum();
        ticketCache.invalidate(redisKey.getId());
        messagePublisher.delete(redisKey.getId());
        return count;
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
                .id(digest(ticket.getId()))
                .principal(digest(userId))
                .prefix(ticket.getPrefix())
                .build();

            val timeout = RedisCompositeKey.getTimeout(ticket);
            val redisKeyPattern = redisKey.toKeyPattern();

            val ticketDocument = buildTicketAsDocument(ticket);
            redisTemplate.boundValueOps(redisKeyPattern).set(ticketDocument, timeout, TimeUnit.SECONDS);

            ticketCache.put(redisKey.getId(), ticket);
            messagePublisher.add(ticket);
        });
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        return FunctionUtils.doAndHandle(() -> {
            val prefix = StringUtils.substring(ticketId, 0, ticketId.indexOf(UniqueTicketIdGenerator.SEPARATOR));
            val redisKey = RedisCompositeKey.builder()
                .id(digest(ticketId))
                .prefix(prefix)
                .build();

            val ticket = Optional.ofNullable(ticketCache.getIfPresent(redisKey.getId()))
                .map(this::decodeTicket)
                .filter(predicate)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    val redisKeyPattern = redisKey.toKeyPattern();
                    return scanKeys(redisKeyPattern)
                        .map(key -> redisTemplate.boundValueOps(key).get())
                        .filter(Objects::nonNull)
                        .map(this::deserializeAsTicket)
                        .map(this::decodeTicket)
                        .filter(predicate)
                        .findFirst()
                        .orElse(null);
                });
            if (ticket != null && predicate.test(ticket) && !ticket.isExpired()) {
                ticketCache.put(redisKey.getId(), ticket);
                return ticket;
            }
            ticketCache.invalidate(redisKey.getId());
            messagePublisher.delete(redisKey.getId());
            return null;
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
            .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .map(this::deserializeAsTicket)
            .map(this::decodeTicket)
            .filter(Objects::nonNull)
            .peek(ticket -> ticketCache.put(ticket.getId(), ticket));
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        return FunctionUtils.doAndHandle(() -> {
            LOGGER.debug("Updating ticket [{}]", ticket);
            val userId = getPrincipalIdFrom(ticket);
            val redisKey = RedisCompositeKey.builder()
                .id(digest(ticket.getId()))
                .principal(digest(userId))
                .prefix(ticket.getPrefix())
                .build();
            val redisKeyPattern = redisKey.toKeyPattern();
            LOGGER.debug("Fetched redis key [{}] for ticket [{}]", redisKeyPattern, ticket);
            val timeout = RedisCompositeKey.getTimeout(ticket);

            val ticketDocument = buildTicketAsDocument(ticket);
            redisTemplate.boundValueOps(redisKeyPattern).set(ticketDocument, timeout, TimeUnit.SECONDS);
            ticketCache.put(ticket.getId(), ticket);
            messagePublisher.update(ticket);
            return ticket;
        });
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val redisKey = RedisCompositeKey.builder()
            .principal(digest(principalId))
            .prefix(TicketGrantingTicket.PREFIX)
            .build()
            .toKeyPattern();
        return scanKeys(redisKey)
            .map(key -> redisTemplate.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .map(this::deserializeAsTicket)
            .map(this::decodeTicket)
            .filter(Objects::nonNull);
    }

    @Override
    public long sessionCount() {
        val redisKey = RedisCompositeKey.builder()
            .prefix(TicketGrantingTicket.PREFIX)
            .build()
            .toKeyPattern();
        return scanKeysAndCount(redisKey);
    }

    @Override
    public long serviceTicketCount() {
        val redisKey = RedisCompositeKey.builder()
            .prefix(ServiceTicket.PREFIX)
            .build()
            .toKeyPattern();
        return scanKeysAndCount(redisKey);
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        return Stream.empty();
    }

    private long scanKeysAndCount(final String redisKey) {
        val keys = scanKeys(redisKey).collect(Collectors.toList());
        return keys.isEmpty() ? 0 : Objects.requireNonNull(redisTemplate.countExistingKeys(keys));
    }

    /**
     * Get a stream of all CAS-related keys from Redis DB.
     *
     * @return stream of all CAS-related keys from Redis DB
     */
    private Stream<String> scanKeys() {
        return scanKeys(RedisCompositeKey.getPatternTicketRedisKey());
    }

    private Stream<String> scanKeys(final String key) {
        LOGGER.debug("Loading keys for pattern [{}]", key);
        return Objects.requireNonNull(redisTemplate.keys(key)).stream();
    }

    protected RedisTicketDocument buildTicketAsDocument(final Ticket ticket) {
        return FunctionUtils.doUnchecked(() -> {
            val encTicket = encodeTicket(ticket);
            val json = serializeTicket(encTicket);
            FunctionUtils.throwIf(StringUtils.isBlank(json),
                () -> new IllegalArgumentException("Ticket " + ticket.getId() + " cannot be serialized to JSON"));
            LOGGER.trace("Serialized ticket into a JSON document as\n [{}]",
                JsonValue.readJSON(json).toString(Stringify.FORMATTED));

            val principal = getPrincipalIdFrom(ticket);
            return RedisTicketDocument.builder()
                .type(encTicket.getClass().getName())
                .ticketId(encTicket.getId())
                .json(json)
                .principal(digest(principal))
                .attributes(collectAndDigestTicketAttributes(ticket))
                .build();
        });
    }

    private void createIndexesIfNecessary() {
        searchCommands.ifPresent(command -> {
            val options = CreateOptions.<String, RedisTicketDocument>builder()//
                .prefix(String.format("%s:", RedisTicketDocument.class.getName())).build();
            val indexId = Field.text(RedisTicketDocument.FIELD_NAME_ID).build();
            val indexPrincipal = Field.text(RedisTicketDocument.FIELD_NAME_PRINCIPAL).build();
            val indexType = Field.text(RedisTicketDocument.FIELD_NAME_TYPE).build();
            val indexAttributes = Field.text(RedisTicketDocument.FIELD_NAME_ATTRIBUTES).build();
            try {
                command.ftCreate(
                    SEARCH_INDEX_NAME, options,
                    indexId, indexPrincipal, indexType, indexAttributes);
            } catch (final RedisCommandExecutionException e) {
                if (!"Index already exists".equalsIgnoreCase(e.getMessage())) {
                    throw e;
                }
            }
        });
    }


    protected Ticket deserializeAsTicket(final RedisTicketDocument document) {
        return ticketSerializationManager.deserializeTicket(document.getJson(), document.getType());
    }
}
