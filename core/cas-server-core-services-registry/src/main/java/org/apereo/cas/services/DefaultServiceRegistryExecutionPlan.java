package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link DefaultServiceRegistryExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Slf4j
public class DefaultServiceRegistryExecutionPlan implements ServiceRegistryExecutionPlan {
    private final List<ServiceRegistry> serviceRegistries = new ArrayList<>();

    @Override
    @CanIgnoreReturnValue
    public ServiceRegistryExecutionPlan registerServiceRegistry(final ServiceRegistry registry) {
        if (BeanSupplier.isNotProxy(registry)) {
            LOGGER.trace("Registering service registry [{}] into the execution plan", registry.getName());
            serviceRegistries.add(registry);
        }
        return this;
    }

    @Override
    public Collection<ServiceRegistry> find(final Predicate<ServiceRegistry> typeFilter) {
        return serviceRegistries
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(typeFilter)
            .collect(Collectors.toList());
    }
}
