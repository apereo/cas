package org.apereo.cas.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultServicesManagerExecutionPlan}.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
@Getter
@Slf4j
public class DefaultServicesManagerExecutionPlan implements ServicesManagerExecutionPlan {
    private final Collection<ServicesManager> servicesManagers = new ArrayList<>();

    @Override
    public ServicesManagerExecutionPlan registerServicesManager(final ServicesManager manager) {
        LOGGER.trace("Registering service registry [{}] into the execution plan", manager);
        servicesManagers.add(manager);
        return this;
    }

    @Override
    public Collection<ServicesManager> find(final Predicate<ServicesManager> typeFilter) {
        return servicesManagers.stream()
            .filter(typeFilter)
            .collect(Collectors.toList());
    }
}
