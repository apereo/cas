package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.IdleExpirationPolicy;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.key.RedisKeyGenerator;
import org.apereo.cas.ticket.registry.key.RedisKeyGeneratorFactory;
import org.apereo.cas.ticket.registry.pub.RedisTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.thread.Cleanable;
import com.github.benmanes.caffeine.cache.Cache;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Document;
import com.redis.lettucemod.search.Field;
import com.redis.lettucemod.search.SearchResults;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.convert.RedisData;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Key-value ticket registry implementation that stores tickets in redis.
 *
 * @author Misagh Moayyed
 * @author Jerome Leleu
 * @since 5.1.0
 */
@Slf4j
@Monitorable
@Getter
public class RedisTicketRegistry extends AbstractTicketRegistry implements Cleanable {

    private static final String SEARCH_INDEX_NAME = RedisTicketDocument.class.getSimpleName() + "Index";

    private final CasRedisTemplates casRedisTemplates;

    private final ObjectProvider<Cache<String, Ticket>> ticketCache;

    private final ObjectProvider<RedisTicketRegistryMessagePublisher> messagePublisher;

    private final Optional<RedisModulesCommands> redisModuleCommands;

    private final RedisKeyGeneratorFactory redisKeyGeneratorFactory;

    private final CasConfigurationProperties casProperties;

    private final RedisKeyValueAdapter redisKeyValueAdapter;

    public RedisTicketRegistry(final CipherExecutor cipherExecutor,
                               final TicketSerializationManager ticketSerializationManager,
                               final TicketCatalog ticketCatalog,
                               final ConfigurableApplicationContext applicationContext,
                               final CasRedisTemplates casRedisTemplates,
                               final ObjectProvider<Cache<String, Ticket>> ticketCache,
                               final ObjectProvider<RedisTicketRegistryMessagePublisher> messagePublisher,
                               final Optional<RedisModulesCommands> redisModuleCommands,
                               final RedisKeyGeneratorFactory redisKeyGeneratorFactory,
                               final RedisKeyValueAdapter redisKeyValueAdapter,
                               final CasConfigurationProperties casProperties) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.casRedisTemplates = casRedisTemplates;
        this.ticketCache = ticketCache;
        this.messagePublisher = messagePublisher;
        this.redisModuleCommands = redisModuleCommands;
        this.redisKeyGeneratorFactory = redisKeyGeneratorFactory;
        this.casProperties = casProperties;
        this.redisKeyValueAdapter = redisKeyValueAdapter;
        createIndexesIfNecessary();
    }

    @Override
    public long deleteAll() {
        val size = new AtomicLong();
        redisKeyGeneratorFactory.getRedisKeyGenerators().forEach(generator -> {
            val keyPattern = generator.forPrefixAndId("*", "*");
            val options = ScanOptions.scanOptions().match(keyPattern).build();
            try (val result = casRedisTemplates.getTicketsRedisTemplate().scan(options)) {
                casRedisTemplates.getTicketsRedisTemplate().executePipelined((RedisCallback<Object>) connection -> {
                    StreamSupport.stream(result.spliterator(), false).forEach(id -> {
                        connection.keyCommands().del(id.getBytes(StandardCharsets.UTF_8));
                        if (generator.isTicketKeyGenerator()) {
                            size.getAndIncrement();
                        }
                    });
                    return null;
                });
            }
        });
        clean();
        return size.get();
    }

    @Override
    public long deleteSingleTicket(final Ticket ticket) {
        val redisKeyGenerator = redisKeyGeneratorFactory.getRedisKeyGenerator(ticket.getPrefix()).orElseThrow();
        val redisTicketsKey = redisKeyGenerator.forPrefixAndId(ticket.getPrefix(), digestIdentifier(ticket.getId()));
        val count = Stream.of(redisTicketsKey)
            .mapToInt(id -> BooleanUtils.toBoolean(casRedisTemplates.getTicketsRedisTemplate().delete(id)) ? 1 : 0)
            .sum();

        if (ticket instanceof TicketGrantingTicket) {
            redisKeyGeneratorFactory.getRedisKeyGenerator(Principal.class.getName())
                .ifPresent(principalGenerator -> {
                    val userId = digestIdentifier(getPrincipalIdFrom(ticket));
                    if (StringUtils.isNotBlank(userId)) {
                        val digestedId = digestIdentifier(ticket.getId());
                        val redisPrincipalKey = principalGenerator.forId(userId);
                        val ops = casRedisTemplates.getSessionsRedisTemplate().boundZSetOps(redisPrincipalKey);
                        ops.remove(digestedId);
                    }
                });
        }

        ticketCache.ifAvailable(cache -> cache.invalidate(redisKeyGenerator.rawKey(redisTicketsKey)));
        messagePublisher.ifAvailable(publisher -> publisher.delete(ticket));
        return count;
    }

    @Override
    public List<? extends Ticket> addTicket(final Stream<? extends Ticket> toSave) {
        return (List) casRedisTemplates.getTicketsRedisTemplate().executePipelined((RedisCallback) connection -> {
            toSave.parallel().forEach(this::addSingleTicket);
            return null;
        });
    }

    @Override
    public Ticket addSingleTicket(final Ticket ticket) {
        LOGGER.debug("Adding ticket [{}]", ticket);
        addOrUpdateTicket(ticket);
        messagePublisher.ifAvailable(publisher -> publisher.add(ticket));
        return ticket;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        FunctionUtils.doIfNotNull(ticket, __ -> {
            LOGGER.debug("Updating ticket [{}]", ticket);
            addOrUpdateTicket(ticket);
            messagePublisher.ifAvailable(p -> p.update(ticket));
        });
        return ticket;
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        return FunctionUtils.doAndHandle(() -> {
            val ticketPrefix = StringUtils.substring(ticketId, 0, ticketId.indexOf(UniqueTicketIdGenerator.SEPARATOR));
            val redisKeyGenerator = redisKeyGeneratorFactory.getRedisKeyGenerator(ticketPrefix).orElseThrow();
            val redisTicketsKey = redisKeyGenerator.forPrefixAndId(ticketPrefix, digestIdentifier(ticketId));
            return getTicketFromRedis(predicate, redisTicketsKey, redisKeyGenerator);
        });
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        try (val ticketsStream = stream()) {
            return ticketsStream.collect(Collectors.toSet());
        }
    }

    @Override
    public Stream<? extends Ticket> stream(final TicketRegistryStreamCriteria criteria) {
        return fetchKeysForTickets()
            .skip(criteria.getFrom())
            .limit(criteria.getCount())
            .map(redisKey -> {
                val compositeKey = RedisKeyGenerator.parse(redisKey);
                val redisKeyGenerator = redisKeyGeneratorFactory.getRedisKeyGenerator(compositeKey.getPrefix()).orElseThrow();
                val keyspace = redisKeyGenerator.getKeyspace();
                val entryId = redisKeyGenerator.rawKey(redisKey);
                val document = redisKeyValueAdapter.get(entryId, keyspace, RedisTicketDocument.class);
                if (document == null) {
                    casRedisTemplates.getTicketsRedisTemplate().delete(redisKey);
                    return null;
                }
                return document;
            })
            .filter(Objects::nonNull)
            .map(RedisTicketDocument.class::cast)
            .map(document -> deserializeTicket(document.json(), document.type()))
            .map(this::decodeTicket)
            .filter(Objects::nonNull)
            .peek(ticket -> {
                if (!ticket.isExpired()) {
                    val redisKeyGenerator = redisKeyGeneratorFactory.getRedisKeyGenerator(ticket.getPrefix()).orElseThrow();
                    val redisTicketsKey = redisKeyGenerator.forPrefixAndId(ticket.getPrefix(), digestIdentifier(ticket.getId()));
                    ticketCache.ifAvailable(cache -> cache.put(redisKeyGenerator.rawKey(redisTicketsKey), ticket));
                }
            });
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        return redisKeyGeneratorFactory.getRedisKeyGenerator(Principal.class.getName())
            .map(generator -> {
                val userId = digestIdentifier(principalId);
                val redisPrincipalKey = generator.forId(userId);
                val members = casRedisTemplates.getSessionsRedisTemplate().boundZSetOps(redisPrincipalKey).range(0, Long.MAX_VALUE);

                return Objects.requireNonNull(members)
                    .parallelStream()
                    .filter(Objects::nonNull)
                    .map(ticketId -> {
                        val redisKeyGenerator = redisKeyGeneratorFactory.getRedisKeyGenerator(TicketGrantingTicket.PREFIX).orElseThrow();
                        val redisTicketsKey = redisKeyGenerator.forPrefixAndId(redisKeyGenerator.getPrefix(), ticketId);
                        return getTicketFromRedis(ticket -> !ticket.isExpired(), redisTicketsKey, redisKeyGenerator);
                    })
                    .map(this::decodeTicket)
                    .filter(Objects::nonNull)
                    .filter(ticket -> !ticket.isExpired());
            })
            .orElseGet(Stream::empty);
    }

    @Override
    public long countSessionsFor(final String principalId) {
        return getSessionsFor(principalId).count();
    }

    @Override
    public long sessionCount() {
        val redisKeyGenerator = redisKeyGeneratorFactory.getRedisKeyGenerator(TicketGrantingTicket.PREFIX).orElseThrow();
        val redisTicketsKey = redisKeyGenerator.forPrefixAndId(redisKeyGenerator.getPrefix(), "*");

        val options = ScanOptions.scanOptions().match(redisTicketsKey).build();
        try (val result = casRedisTemplates.getTicketsRedisTemplate().scan(options)) {
            return result.stream().parallel().count();
        }
    }

    @Override
    public long serviceTicketCount() {
        val redisTicketsKey = redisKeyGeneratorFactory.getRedisKeyGenerator(ServiceTicket.PREFIX)
            .orElseThrow().forPrefixAndId(ServiceTicket.PREFIX, "*");
        val options = ScanOptions.scanOptions().match(redisTicketsKey).build();
        try (val result = casRedisTemplates.getTicketsRedisTemplate().scan(options)) {
            return result.stream().parallel().count();
        }
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        return redisModuleCommands
            .map(command -> {
                val criteria = new ArrayList<String>();
                queryAttributes.forEach((key, value) -> value.forEach(queryValue -> {
                    val escapedValue = isCipherExecutorEnabled()
                        ? digestIdentifier(queryValue.toString())
                        : StringUtils.replace(queryValue.toString(), "-", "\\-");
                    criteria.add(String.format("(%s" + (isCipherExecutorEnabled() ? " " : "_") + "*%s)", digestIdentifier(key), escapedValue));
                }));
                val query = String.format("(%s) @%s:%s", String.join("|", criteria),
                    RedisTicketDocument.FIELD_NAME_PREFIX, TicketGrantingTicket.PREFIX);
                LOGGER.debug("Executing search query [{}]", query);
                val results = (SearchResults<String, Document>) command.ftSearch(SEARCH_INDEX_NAME, query);
                return results
                    .parallelStream()
                    .map(Document.class::cast)
                    .filter(document -> !document.isEmpty())
                    .map(RedisTicketDocument::from)
                    .filter(document -> StringUtils.isNotBlank(document.json()))
                    .map(redisDoc -> {
                        val ticket = deserializeTicket(redisDoc.json(), redisDoc.type());
                        return decodeTicket(ticket);
                    })
                    .filter(ticket -> !ticket.isExpired());
            })
            .orElseGet(() -> (Stream<Ticket>) super.getSessionsWithAttributes(queryAttributes));
    }

    @Override
    public long countTicketsFor(final Service service) {
        return redisModuleCommands
            .map(command -> {
                val originalUrl = URI.create(service.getOriginalUrl());
                val host = String.format("%s?//%s", originalUrl.getScheme(), originalUrl.getHost());
                val query = String.format("@%s:\"%s\"", RedisTicketDocument.FIELD_NAME_SERVICE, host);
                val results = (SearchResults<String, Document>) command.ftSearch(SEARCH_INDEX_NAME, query);
                return results
                    .parallelStream()
                    .map(Document.class::cast)
                    .filter(document -> !document.isEmpty())
                    .map(RedisTicketDocument::from)
                    .filter(document -> StringUtils.isNotBlank(document.json()))
                    .map(redisDoc -> {
                        val ticket = deserializeTicket(redisDoc.json(), redisDoc.type());
                        return decodeTicket(ticket);
                    })
                    .filter(ticket -> !ticket.isExpired())
                    .count();
            })
            .orElseGet(() -> super.countTicketsFor(service));
    }

    @Override
    public long countTickets() {
        val redisKeyGenerator = redisKeyGeneratorFactory.getRedisKeyGenerator(TicketGrantingTicket.PREFIX).orElseThrow();
        val redisTicketsKey = redisKeyGenerator.forPrefixAndId("*", "*");
        return casRedisTemplates.getTicketsRedisTemplate().count(redisTicketsKey);
    }

    @Override
    public List<? extends Serializable> query(final TicketRegistryQueryCriteria queryCriteria) {
        val redisKeyGenerator = redisKeyGeneratorFactory.getRedisKeyGenerator(queryCriteria.getType()).orElseThrow();
        val redisTicketsKey = StringUtils.isNotBlank(queryCriteria.getId())
            ? redisKeyGenerator.forPrefixAndId(queryCriteria.getType(), digestIdentifier(queryCriteria.getId()))
            : redisKeyGenerator.forPrefixAndId(queryCriteria.getType(), "*");

        if (queryCriteria.isDecode()) {
            try (val scanResults = casRedisTemplates.getTicketsRedisTemplate().scan(redisTicketsKey, queryCriteria.getCount())) {
                return scanResults
                    .parallel()
                    .map(key -> {
                        val ticket = ticketCache.stream().parallel()
                            .map(cache -> cache.getIfPresent(redisKeyGenerator.rawKey(key)))
                            .filter(Objects::nonNull);
                        return ticket.findFirst().orElseGet(() -> {
                            val keyspace = redisKeyGenerator.getKeyspace();
                            val entryId = redisKeyGenerator.rawKey(key);
                            val redisDocument = redisKeyValueAdapter.get(entryId, keyspace, RedisTicketDocument.class);
                            return Stream.ofNullable(redisDocument)
                                .parallel()
                                .filter(Objects::nonNull)
                                .map(document -> deserializeTicket(document.json(), document.type()))
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null);
                        });
                    })
                    .filter(Objects::nonNull)
                    .map(this::decodeTicket)
                    .filter(ticket -> StringUtils.isBlank(queryCriteria.getPrincipal())
                        || (ticket instanceof final AuthenticationAwareTicket aat
                        && StringUtils.equalsIgnoreCase(queryCriteria.getPrincipal(), aat.getAuthentication().getPrincipal().getId())))
                    .filter(ticket -> !ticket.isExpired())
                    .peek(ticket -> {
                        val cacheKey = redisKeyGenerator.forPrefixAndId(ticket.getPrefix(), digestIdentifier(ticket.getId()));
                        ticketCache.ifAvailable(c -> c.put(redisKeyGenerator.rawKey(cacheKey), ticket));
                    })
                    .collect(Collectors.toList());
            }
        }
        val keys = fetchKeysForTickets(redisTicketsKey);
        return (queryCriteria.getCount() > 0 ? keys.limit(queryCriteria.getCount()) : keys).collect(Collectors.toList());
    }

    @Override
    public void clean() {
        ticketCache.ifAvailable(Cache::invalidateAll);
        messagePublisher.ifAvailable(RedisTicketRegistryMessagePublisher::deleteAll);
    }

    private Stream<String> fetchKeysForTickets() {
        val redisKeyGenerator = redisKeyGeneratorFactory.getRedisKeyGenerator(TicketGrantingTicket.PREFIX).orElseThrow();
        val redisKey = redisKeyGenerator.forPrefixAndId("*", "*");
        return fetchKeysForTickets(redisKey);
    }

    private Stream<String> fetchKeysForTickets(final String key) {
        LOGGER.debug("Loading keys for pattern [{}]", key);
        return Objects.requireNonNull(casRedisTemplates.getTicketsRedisTemplate().keys(key)).parallelStream();
    }

    protected RedisTicketDocument buildTicketAsDocument(final Ticket ticket) {
        return FunctionUtils.doUnchecked(() -> {
            val encTicket = encodeTicket(ticket);
            val json = serializeTicket(encTicket);
            FunctionUtils.throwIf(StringUtils.isBlank(json),
                () -> new IllegalArgumentException("Ticket %s cannot be serialized to JSON".formatted(ticket.getId())));

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Serialized ticket into a JSON document as\n [{}]",
                    JsonValue.readJSON(json).toString(Stringify.FORMATTED));
            }

            val principal = getPrincipalIdFrom(ticket);
            val attributeMap = (Map<String, Object>) collectAndDigestTicketAttributes(ticket);
            val attributesEncoded = attributeMap
                .entrySet()
                .parallelStream()
                .map(entry -> {
                    val entryValues = (List) entry.getValue();
                    val valueList = entryValues.parallelStream().map(Object::toString).collect(Collectors.joining(","));
                    return entry.getKey() + (isCipherExecutorEnabled() ? " " : "_") + valueList;
                })
                .collect(Collectors.joining(","));

            return RedisTicketDocument
                .builder()
                .type(encTicket.getClass().getName())
                .ticketId(encTicket.getId())
                .json(json)
                .prefix(ticket.getPrefix())
                .principal(digestIdentifier(principal))
                .attributes(attributesEncoded)
                .service(ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService()) ? sat.getService().getId() : null)
                .build();
        });
    }

    protected Ticket getTicketFromRedis(final Predicate<Ticket> predicate, final String redisKeyPattern,
                                        final RedisKeyGenerator redisKeyGenerator) {
        val rawTicketId = redisKeyGenerator.rawKey(redisKeyPattern);
        val cachedTicket = ticketCache.stream().parallel().map(cache -> cache.getIfPresent(rawTicketId)).filter(Objects::nonNull);

        val ticket = cachedTicket
            .findFirst()
            .map(this::decodeTicket)
            .filter(predicate)
            .stream()
            .findFirst()
            .orElseGet(() -> Stream.of(redisKeyPattern)
                .map(key -> {
                    val keyspace = redisKeyGenerator.getKeyspace();
                    return redisKeyValueAdapter.get(rawTicketId, keyspace, RedisTicketDocument.class);
                })
                .filter(Objects::nonNull)
                .map(document -> deserializeTicket(document.json(), document.type()))
                .map(this::decodeTicket)
                .filter(predicate)
                .findFirst()
                .orElseGet(() -> handleMissingTicket(rawTicketId, redisKeyPattern)));
        
        if (ticket != null && predicate.test(ticket) && !ticket.isExpired()) {
            ticketCache.ifAvailable(cache -> cache.put(rawTicketId, ticket));
            return ticket;
        }
        ticketCache.ifAvailable(cache -> cache.invalidate(rawTicketId));
        messagePublisher.ifAvailable(publisher -> publisher.delete(ticket));
        return null;
    }

    protected Ticket handleMissingTicket(final String rawTicketId, final String redisKey) {
        return null;
    }

    private void addOrUpdateTicket(final Ticket ticket) {
        val digestedId = digestIdentifier(ticket.getId());
        val redisKeyGenerator = redisKeyGeneratorFactory.getRedisKeyGenerator(ticket.getPrefix()).orElseThrow();
        val redisKeyPattern = redisKeyGenerator.forPrefixAndId(ticket.getPrefix(), digestedId);

        val timeout = RedisKeyGenerator.getTicketExpirationInSeconds(ticket);
        val ticketDocument = buildTicketAsDocument(ticket);

        val valueOps = casRedisTemplates.getTicketsRedisTemplate().boundValueOps(redisKeyPattern);
        valueOps.set(ticketDocument, timeout, TimeUnit.SECONDS);

        val keyspace = redisKeyGenerator.getKeyspace();
        val redisDataItem = new RedisData();
        redisKeyValueAdapter.getConverter().write(ticketDocument, redisDataItem);
        redisDataItem.setKeyspace(keyspace);

        redisKeyValueAdapter.put(ticketDocument.ticketId(), redisDataItem, keyspace);
        configureTicketExpirationInstant(ticket, redisKeyPattern);
        ticketCache.ifAvailable(cache -> cache.put(digestedId, ticket));

        redisKeyGeneratorFactory.getRedisKeyGenerator(Principal.class.getName())
            .ifPresent(generator -> trackAuthenticationPrincipal(ticket));
    }

    protected void trackAuthenticationPrincipal(final Ticket ticket) {
        val userId = digestIdentifier(getPrincipalIdFrom(ticket));
        if (StringUtils.isNotBlank(userId) && ticket instanceof TicketGrantingTicket) {
            val generator = redisKeyGeneratorFactory.getRedisKeyGenerator(Principal.class.getName()).orElseThrow();
            val redisPrincipalPattern = generator.forId(userId);
            val ops = casRedisTemplates.getSessionsRedisTemplate().boundZSetOps(redisPrincipalPattern);
            val now = Instant.now(Clock.systemUTC());
            if (casProperties.getTicket().getTgt().getCore().isOnlyTrackMostRecentSession()) {
                ops.expireAt(now);
            } else {
                ops.removeRangeByScore(0, Long.valueOf(now.getEpochSecond()).doubleValue() + 1);
            }
            val timeout = RedisKeyGenerator.getTicketExpirationInSeconds(ticket);
            val digestedId = digestIdentifier(ticket.getId());
            ops.add(digestedId, Long.valueOf(now.getEpochSecond() + timeout).doubleValue());

            if (ticket.getExpirationPolicy() instanceof final IdleExpirationPolicy iep) {
                ops.expireAt(iep.getIdleExpirationTime(ticket).toInstant());
            } else {
                ops.expire(timeout, TimeUnit.SECONDS);
            }
        }
    }

    protected void configureTicketExpirationInstant(final Ticket ticket, final String redisKeyPattern) {
        if (ticket.getExpirationPolicy() instanceof final IdleExpirationPolicy iep) {
            val expirationInstant = iep.getIdleExpirationTime(ticket).toInstant();
            casRedisTemplates.getTicketsRedisTemplate().expireAt(redisKeyPattern, expirationInstant);
            LOGGER.debug("Ticket [{}] will expire at [{}]", ticket.getId(), expirationInstant);
        } else {
            val timeoutSeconds = RedisKeyGenerator.getTicketExpirationInSeconds(ticket);
            casRedisTemplates.getTicketsRedisTemplate().expire(redisKeyPattern, timeoutSeconds, TimeUnit.SECONDS);
            LOGGER.debug("Ticket [{}] will expire in [{}] second(s)", ticket.getId(), timeoutSeconds);
        }
    }

    private void createIndexesIfNecessary() {
        val indexesOnNamespaces = new HashSet<String>();
        redisModuleCommands.ifPresent(command ->
            redisKeyGeneratorFactory.getRedisKeyGenerators()
                .stream()
                .filter(RedisKeyGenerator::isTicketKeyGenerator)
                .forEach(redisKeyGenerator -> {
                    val prefix = redisKeyGenerator.getNamespace() + ':';
                    val options = CreateOptions.<String, RedisTicketDocument>builder()
                        .prefix(prefix)
                        .maxTextFields(true)
                        .build();
                    val createIndex = !indexesOnNamespaces.contains(prefix)
                        && command.ftList().parallelStream().noneMatch(idx -> SEARCH_INDEX_NAME.equalsIgnoreCase(idx.toString()));
                    if (createIndex) {
                        val indexFields = CollectionUtils.wrapList(
                            Field.text(RedisTicketDocument.FIELD_NAME_ID).build(),
                            Field.text(RedisTicketDocument.FIELD_NAME_ATTRIBUTES).build(),
                            Field.text(RedisTicketDocument.FIELD_NAME_PRINCIPAL).build(),
                            Field.text(RedisTicketDocument.FIELD_NAME_TYPE).build(),
                            Field.text(RedisTicketDocument.FIELD_NAME_SERVICE).build(),
                            Field.text(RedisTicketDocument.FIELD_NAME_PREFIX).build());
                        command.ftCreate(SEARCH_INDEX_NAME, options, indexFields.toArray(new Field[]{}));
                        indexesOnNamespaces.add(prefix);
                    }
                }));
    }

    @Data
    public static class CasRedisTemplates {
        private final CasRedisTemplate<String, RedisTicketDocument> ticketsRedisTemplate;

        private final CasRedisTemplate<String, String> sessionsRedisTemplate;
    }

}
