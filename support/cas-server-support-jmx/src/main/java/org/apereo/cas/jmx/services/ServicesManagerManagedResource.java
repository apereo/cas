package org.apereo.cas.jmx.services;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This is {@link ServicesManagerManagedResource}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ManagedResource(description = "Registered service inventory and reload operations")
@RequiredArgsConstructor
public class ServicesManagerManagedResource {
    private final ServicesManager servicesManager;

    /**
     * Gets services.
     *
     * @return the services
     */
    @ManagedOperation(description = "List loaded services as id-name:serviceId")
    public Collection<String> getServices() {
        try (val stream = servicesManager.stream()) {
            return stream
                .map(service -> String.format("%s-%s:%s", service.getId(), service.getName(), service.getServiceId()))
                .collect(Collectors.toCollection(TreeSet::new));
        }
    }

    /**
     * Gets service count.
     *
     * @return the service count
     */
    @ManagedAttribute(description = "Number of currently loaded services")
    public long getServiceCount() {
        try (val stream = servicesManager.stream()) {
            return stream.count();
        }
    }

    /**
     * Find a registered service by its numeric identifier.
     *
     * @param id the registered service identifier
     * @return the service summary, or an empty string if no service is found
     */
    @ManagedOperation(description = "Find a service by numeric id; returns an empty string when absent")
    @ManagedOperationParameter(name = "id", description = "Registered service numeric identifier")
    public String getService(final long id) {
        val service = servicesManager.findServiceBy(id);
        return service == null ? StringUtils.EMPTY
            : String.format("%s-%s:%s", service.getId(), service.getName(), service.getServiceId());
    }

    /**
     * Reload registered services through the services manager.
     *
     * @return the number of reloaded services
     */
    @ManagedOperation(description = "Reload services from configured registries and return the loaded count")
    public int reload() {
        return servicesManager.load().size();
    }
}
