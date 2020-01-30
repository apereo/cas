package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.domain.DomainServicesManager;

import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Class that chains services managers together.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
public class ChainingServicesManager implements ServicesManager {

    private List<ServicesManager> serviceManagers;

    public ChainingServicesManager() {
        this.serviceManagers = new ArrayList();
    }

    /**
     * Adds a services manager to the chain.
     * @param manager - a services manager
     */
    public void addServiceManager(final ServicesManager manager) {
        this.serviceManagers.add(manager);
    }

    private Optional<ServicesManager> getManager(final RegisteredService service) {
        return serviceManagers.stream().filter(s -> s.supports(service)).findFirst();
    }

    private Optional<ServicesManager> getManager(final Service service) {
        return serviceManagers.stream().filter(s -> s.supports(service)).findFirst();
    }

    private Optional<ServicesManager> getManager(final Class clazz) {
        return serviceManagers.stream().filter(s -> s.supports(clazz)).findFirst();
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        val manager = getManager(registeredService);
        return manager.isPresent() ? manager.get().save(registeredService) : null;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService, final boolean publishEvent) {
        val manager = getManager(registeredService);
        return manager.isPresent() ? manager.get().save(registeredService, publishEvent) : null;
    }

    @Override
    public void deleteAll() {
        serviceManagers.forEach(s -> s.deleteAll());
    }

    @Override
    public void deleteAllByType(final Service service) {
        val manager = getManager(service);
        if (manager.isPresent()) {
            manager.get().deleteAll();
        }
    }

    @Override
    public RegisteredService delete(final long id) {
        return serviceManagers.stream()
                .map(s -> s.delete(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public RegisteredService delete(final RegisteredService svc) {
        val manager = getManager(svc);
        return manager.isPresent() ? manager.get().delete(svc) : null;
    }

    @Override
    public RegisteredService findServiceBy(final String serviceId) {
        return serviceManagers.stream()
                .map(s -> s.findServiceBy(serviceId))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public RegisteredService findServiceBy(final Service service) {
        val manager = getManager(service);
        return manager.isPresent() ? manager.get().findServiceBy(service) : null;
    }

    @Override
    public Collection<RegisteredService> findServiceBy(final Predicate<RegisteredService> clazz) {
        return serviceManagers.stream()
                .flatMap(s -> s.findServiceBy(clazz).stream())
                .collect(Collectors.toList());

    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final Service serviceId, final Class<T> clazz) {
        val manager = getManager(serviceId);
        return manager.isPresent() ? manager.get().findServiceBy(serviceId, clazz) : null;
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final String serviceId, final Class<T> clazz) {
        val manager = getManager(clazz);
        return manager.isPresent() ? manager.get().findServiceBy(serviceId, clazz) : null;
    }

    @Override
    public RegisteredService findServiceBy(final long id) {
        return serviceManagers.stream()
                .map(s -> s.findServiceBy(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return serviceManagers.stream()
                .flatMap(s -> s.getAllServices().stream())
                .collect(toList());
    }

    @Override
    public Collection<RegisteredService> getAllServicesByType(final Service service) {
        val manager = getManager(service);
        return manager.isPresent() ? manager.get().getAllServices() : null;
    }

    @Override
    public Collection<RegisteredService> load() {
        return serviceManagers.stream()
                .flatMap(s -> s.load().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<RegisteredService> getServicesForDomain(final String domain) {
        return serviceManagers.stream()
                .filter(m -> m instanceof DomainServicesManager)
                .flatMap(d -> d.getServicesForDomain(domain).stream())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getDomains() {
        return serviceManagers.stream()
                .filter(m -> m instanceof DomainServicesManager)
                .flatMap(d -> d.getDomains().stream())
                .collect(Collectors.toList());
    }
}
