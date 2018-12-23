package org.apereo.cas.support.events.dao;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is {@link InMemoryCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class InMemoryCasEventRepository extends AbstractCasEventRepository {
    private final LoadingCache<String, CasEvent> cache;

    @Override
    public void save(final CasEvent event) {
        cache.put(UUID.randomUUID().toString(), event);
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
}
