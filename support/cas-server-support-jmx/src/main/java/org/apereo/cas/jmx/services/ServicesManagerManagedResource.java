package org.apereo.cas.jmx.services;

import org.apereo.cas.services.ServicesManager;

import lombok.RequiredArgsConstructor;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link ServicesManagerManagedResource}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ManagedResource
@RequiredArgsConstructor
public class ServicesManagerManagedResource {
    private final ServicesManager servicesManager;

    @ManagedOperation
    public Collection<String> getServices() {
        return servicesManager.getAllServicesStream()
            .map(service -> String.format("%s-%s:%s", service.getId(), service.getName(), service.getServiceId()))
            .collect(Collectors.toSet());
    }
}
