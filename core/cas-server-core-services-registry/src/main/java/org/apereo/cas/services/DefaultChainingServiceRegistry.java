package org.apereo.cas.services;

import com.google.common.base.Predicates;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultChainingServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Slf4j
public class DefaultChainingServiceRegistry extends AbstractServiceRegistry implements ChainingServiceRegistry {
    private final List<ServiceRegistry> serviceRegistries;

    public DefaultChainingServiceRegistry(final ConfigurableApplicationContext applicationContext) {
        this(applicationContext, new ArrayList<>(0));
    }

    public DefaultChainingServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                          final List<ServiceRegistry> serviceRegistries) {
        super(applicationContext, new ArrayList<>(0));
        this.serviceRegistries = serviceRegistries;
    }

    @Override
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

    @Override
    public long countServiceRegistries() {
        return this.serviceRegistries.size();
    }

    @Override
    public void synchronize(final RegisteredService service) {
        this.serviceRegistries
            .stream()
            .filter(serviceRegistry -> {
                if (StringUtils.isNotBlank(service.getServiceId())) {
                    val match = serviceRegistry.findServiceByExactServiceId(service.getServiceId());
                    if (match != null) {
                        LOGGER.debug("Skipping [{}] JSON service definition in [{}] as a matching service [{}] is found in the registry",
                            service.getName(), serviceRegistry.getName(), match.getName());
                        return false;
                    }
                }
                val match = serviceRegistry.findServiceById(service.getId());
                if (match != null) {
                    LOGGER.debug("Skipping [{}] JSON service definition in [{}] as a matching id [{}] is found in the registry",
                        service.getName(), serviceRegistry.getName(), match.getId());
                    return false;
                }
                return true;
            })
            .forEach(serviceRegistry -> serviceRegistry.save(service));
    }
}
