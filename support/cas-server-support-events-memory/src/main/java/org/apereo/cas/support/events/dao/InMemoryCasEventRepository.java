package org.apereo.cas.support.events.dao;

import org.apereo.cas.support.events.CasEventRepositoryFilter;

import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Collection<? extends CasEvent> load() {
        return cache.asMap().values();
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id) {
        return cache
            .asMap()
            .values()
            .stream()
            .filter(e -> e.getPrincipalId().equalsIgnoreCase(id))
            .collect(Collectors.toSet());
    }

    @Override
    public void saveInternal(final CasEvent event) {
        cache.put(UUID.randomUUID().toString(), event);
    }
}
