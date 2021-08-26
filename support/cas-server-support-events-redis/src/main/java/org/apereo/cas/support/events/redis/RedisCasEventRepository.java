package org.apereo.cas.support.events.redis;

import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final RedisTemplate<String, CasEvent> template;

    public RedisCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter,
                                   final RedisTemplate<String, CasEvent> redisTemplate) {
        super(eventRepositoryFilter);
        this.template = redisTemplate;
    }

    private static String getKey(final String type, final String principal, final String timestamp) {
        return CAS_PREFIX + KEY_SEPARATOR + type + KEY_SEPARATOR + principal + KEY_SEPARATOR + timestamp;
    }

    @Override
    public Collection<CasEvent> load() {
        val keys = getKeys("*", "*", "*");
        return keys
            .stream()
            .map(key -> this.template.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends CasEvent> load(final ZonedDateTime dateTime) {
        val keys = getKeys("*", "*", "*");
        return keys
            .stream()
            .map(key -> this.template.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .filter(event -> event.getTimestamp() >= dateTime.toInstant().toEpochMilli())
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        val keys = getKeys(type, principal, "*");
        return keys
            .stream()
            .map(key -> this.template.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type,
                                                                      final String principal,
                                                                      final ZonedDateTime dateTime) {
        val keys = getKeys(type, principal, "*");
        return keys
            .stream()
            .map(key -> this.template.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .filter(event -> event.getTimestamp() >= dateTime.toInstant().toEpochMilli())
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfType(final String type) {
        val keys = getKeys(type, "*", "*");
        return keys
            .stream()
            .map(key -> this.template.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        val keys = getKeys(type, "*", "*");
        return keys
            .stream()
            .map(key -> this.template.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .filter(event -> event.getTimestamp() >= dateTime.toInstant().toEpochMilli())
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id) {
        val keys = getKeys("*", id, "*");
        return keys
            .stream()
            .map(key -> this.template.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String principal, final ZonedDateTime dateTime) {
        val keys = getKeys("*", principal, "*");
        return keys
            .stream()
            .map(key -> this.template.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .filter(event -> event.getTimestamp() >= dateTime.toInstant().toEpochMilli())
            .collect(Collectors.toList());
    }

    @Override
    public CasEvent saveInternal(final CasEvent event) {
        val key = getKey(event.getType(), event.getPrincipalId(), String.valueOf(event.getTimestamp()));
        LOGGER.trace("Saving event record based on key [{}]", key);
        val ops = this.template.boundValueOps(key);
        ops.set(event);
        return event;
    }

    private Set<String> getKeys(final String type, final String principal, final String timestamp) {
        val key = getKey(type, principal, timestamp);
        LOGGER.trace("Fetching records based on key [{}]", key);
        return this.template.keys(key);
    }
}
