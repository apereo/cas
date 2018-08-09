package org.apereo.cas.uma.ticket.resource.repository;

import org.apereo.cas.uma.ticket.resource.ResourceSet;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultResourceSetRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class DefaultResourceSetRepository implements ResourceSetRepository {
    private final Map<Long, ResourceSet> repository = new ConcurrentHashMap<>();

    @Override
    public Collection<ResourceSet> getAll() {
        return repository.values();
    }

    @Override
    public Optional<ResourceSet> getById(final long id) {
        return Optional.ofNullable(repository.get(id));
    }

    @Override
    public Collection<ResourceSet> getByOwner(final String owner) {
        return repository.values().stream().filter(s -> s.getOwner().equalsIgnoreCase(owner)).collect(Collectors.toSet());
    }

    @Override
    public Collection<ResourceSet> getByClient(final String clientId) {
        return repository.values().stream().filter(s -> s.getClientId().equalsIgnoreCase(clientId)).collect(Collectors.toSet());
    }

    @Override
    public ResourceSet save(final ResourceSet set) {
        repository.put(set.getId(), set);
        return set;
    }

    @Override
    public void remove(final ResourceSet set) {
        repository.remove(set.getId());
    }

    @Override
    public void removeAll() {
        repository.clear();
    }

    @Override
    public long count() {
        return repository.size();
    }
}
