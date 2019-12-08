package org.apereo.cas.uma.ticket.resource.repository.impl;

import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.repository.BaseResourceSetRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link DefaultResourceSetRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class DefaultResourceSetRepository extends BaseResourceSetRepository {
    private final Map<Long, ResourceSet> repository = new ConcurrentHashMap<>();

    @Override
    public ResourceSet saveInternal(final ResourceSet set) {
        if (set.getId() <= 0) {
            set.setId(System.currentTimeMillis());
        }
        repository.put(set.getId(), set);
        return set;
    }

    @Override
    public Collection<ResourceSet> getAll() {
        return repository.values();
    }

    @Override
    public Optional<ResourceSet> getById(final long id) {
        return Optional.ofNullable(repository.get(id));
    }

    @Override
    public void remove(final ResourceSet set) {
        repository.remove(set.getId());
    }

    @Override
    public void removeAll() {
        repository.clear();
    }
}
