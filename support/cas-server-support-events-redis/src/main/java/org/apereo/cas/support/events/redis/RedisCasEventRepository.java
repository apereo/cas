package org.apereo.cas.support.events.redis;

import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This is {@link RedisCasEventRepository} that stores event data into a redis database.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ToString
@Getter
@Slf4j
public class RedisCasEventRepository extends AbstractCasEventRepository {
    private static final String KEY_SEPARATOR = ":";

    private static final String CAS_PREFIX = RedisCasEventRepository.class.getSimpleName();

    private final CasRedisTemplate<String, CasEvent> template;

    private final long scanCount;

    public RedisCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter,
                                   final CasRedisTemplate<String, CasEvent> redisTemplate,
                                   final long scanCount) {
        super(eventRepositoryFilter);
        this.template = redisTemplate;
        this.scanCount = scanCount;
    }

    private static String getKey(final String type, final String principal, final String timestamp) {
        return CAS_PREFIX + KEY_SEPARATOR + type + KEY_SEPARATOR + principal + KEY_SEPARATOR + timestamp;
    }

    @Override
    public void removeAll() {
        try (val keys = getKeys("*", "*", "*")) {
            keys.forEach(template::delete);
        }
    }

    @Override
    public Stream<? extends CasEvent> load() {
        try (val keys = getKeys("*", "*", "*")) {
            return keys
                .map(key -> this.template.boundValueOps(key).get())
                .filter(Objects::nonNull)
                .toList()
                .stream();
        }
    }

    @Override
    public Stream<? extends CasEvent> load(final ZonedDateTime dateTime) {
        try (val keys = getKeys("*", "*", "*")) {
            return keys
                .map(key -> this.template.boundValueOps(key).get())
                .filter(Objects::nonNull)
                .filter(event -> event.getTimestamp() >= dateTime.toInstant().toEpochMilli())
                .toList()
                .stream();
        }
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        try (val keys = getKeys(type, principal, "*")) {
            return keys
                .map(key -> this.template.boundValueOps(key).get())
                .filter(Objects::nonNull)
                .toList()
                .stream();
        }
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type,
                                                                  final String principal,
                                                                  final ZonedDateTime dateTime) {
        try (val keys = getKeys(type, principal, "*")) {
            return keys
                .map(key -> this.template.boundValueOps(key).get())
                .filter(Objects::nonNull)
                .filter(event -> event.getTimestamp() >= dateTime.toInstant().toEpochMilli())
                .toList()
                .stream();
        }
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfType(final String type) {
        try (val keys = getKeys(type, "*", "*")) {
            return keys
                .map(key -> this.template.boundValueOps(key).get())
                .filter(Objects::nonNull)
                .toList()
                .stream();
        }
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        try (val keys = getKeys(type, "*", "*")) {
            return keys
                .map(key -> this.template.boundValueOps(key).get())
                .filter(Objects::nonNull)
                .filter(event -> event.getTimestamp() >= dateTime.toInstant().toEpochMilli())
                .toList()
                .stream();
        }
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String id) {
        try (val keys = getKeys("*", id, "*")) {
            return keys
                .map(key -> this.template.boundValueOps(key).get())
                .filter(Objects::nonNull)
                .toList()
                .stream();
        }
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String principal, final ZonedDateTime dateTime) {
        try (val keys = getKeys("*", principal, "*")) {
            return keys
                .map(key -> this.template.boundValueOps(key).get())
                .filter(Objects::nonNull)
                .filter(event -> event.getTimestamp() >= dateTime.toInstant().toEpochMilli())
                .toList()
                .stream();
        }
    }

    @Override
    public CasEvent saveInternal(final CasEvent event) {
        event.assignIdIfNecessary();
        val key = getKey(event.getType(), event.getPrincipalId(), String.valueOf(event.getTimestamp()));
        LOGGER.trace("Saving event record based on key [{}]", key);
        val ops = this.template.boundValueOps(key);
        ops.set(event);
        return event;
    }

    private Stream<String> getKeys(final String type, final String principal, final String timestamp) {
        val key = getKey(type, principal, timestamp);
        LOGGER.trace("Fetching records based on key [{}]", key);
        return template.scan(key, this.scanCount);
    }
}
