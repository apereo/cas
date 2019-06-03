package org.apereo.cas.services;

import com.google.common.base.Predicates;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
public class ChainingServiceRegistry extends AbstractServiceRegistry {
    private final List<ServiceRegistry> serviceRegistries;

    public ChainingServiceRegistry(final ApplicationEventPublisher eventPublisher) {
        this(eventPublisher, new ArrayList<>());
    }

    public ChainingServiceRegistry(final ApplicationEventPublisher eventPublisher,
                                   final List<ServiceRegistry> serviceRegistries) {
        super(eventPublisher, new ArrayList<>());
        this.serviceRegistries = serviceRegistries;
    }

    /**
     * Add service registry.
     *
     * @param registry the registry
     */
    public void addServiceRegistry(final ServiceRegistry registry) {
        serviceRegistries.add(registry);
    }

    /**
     * Add service registries.
     *
     * @param registries the registries
     */
    public void addServiceRegistries(final Collection<ServiceRegistry> registries) {
        serviceRegistries.addAll(registries);
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        serviceRegistries.forEach(registry -> registry.save(registeredService));
        return registeredService;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return serviceRegistries.stream()
            .map(registry -> registry.delete(registeredService))
            .filter(Boolean::booleanValue)
            .findAny()
            .orElse(Boolean.FALSE);
    }

    @Override
    public Collection<RegisteredService> load() {
        return serviceRegistries.stream()
            .map(ServiceRegistry::load)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return serviceRegistries.stream()
            .map(registry -> registry.findServiceById(id))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        return serviceRegistries.stream()
            .map(registry -> registry.findServiceById(id))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public RegisteredService findServiceByExactServiceId(final String id) {
        return serviceRegistries.stream()
            .map(registry -> registry.findServiceByExactServiceId(id))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public RegisteredService findServiceByExactServiceName(final String name) {
        return serviceRegistries.stream()
            .map(registry -> registry.findServiceByExactServiceName(name))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public long size() {
        val filter = Predicates.not(Predicates.instanceOf(ImmutableServiceRegistry.class));
        return serviceRegistries.stream()
            .filter(filter)
            .map(ServiceRegistry::size)
            .mapToLong(Long::longValue)
            .sum();
    }

    @Override
    public String getName() {
        val filter = Predicates.not(Predicates.instanceOf(ImmutableServiceRegistry.class));
        val name = serviceRegistries.stream()
            .filter(filter)
            .map(ServiceRegistry::getName)
            .collect(Collectors.joining(","));
        return StringUtils.defaultIfBlank(name, getClass().getSimpleName());
    }
}
