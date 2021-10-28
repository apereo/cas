package org.apereo.cas.support.events.dao;

import org.apereo.cas.support.events.CasEventRepositoryFilter;

import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.UUID;
import java.util.stream.Stream;

/**
 * This is {@link InMemoryCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class InMemoryCasEventRepository extends AbstractCasEventRepository {
    private final LoadingCache<String, CasEvent> cache;

    public InMemoryCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter,
                                      final LoadingCache<String, CasEvent> cache) {
        super(eventRepositoryFilter);
        this.cache = cache;
    }

    @Override
    public Stream<? extends CasEvent> load() {
        return cache.asMap().values().stream();
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String id) {
        return cache
            .asMap()
            .values()
            .stream()
            .filter(e -> e.getPrincipalId().equalsIgnoreCase(id));
    }

    @Override
    public CasEvent saveInternal(final CasEvent event) {
        cache.put(UUID.randomUUID().toString(), event);
        return event;
    }
}
