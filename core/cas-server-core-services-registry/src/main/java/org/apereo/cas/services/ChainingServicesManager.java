package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.domain.DomainServicesManager;

import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingServicesManager}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
public class ChainingServicesManager implements ServicesManager {

    private final List<ServicesManager> serviceManagers = new ArrayList<>();

    /**
     * Adds a services manager to the chain.
     *
     * @param manager - a services manager
     */
    public void registerServiceManager(final ServicesManager manager) {
        this.serviceManagers.add(manager);
    }

    private Optional<ServicesManager> findServicesManager(final RegisteredService service) {
        return serviceManagers.stream().filter(s -> s.supports(service)).findFirst();
    }

    private Optional<ServicesManager> findServicesManager(final Service service) {
        return serviceManagers.stream().filter(s -> s.supports(service)).findFirst();
    }

    private Optional<ServicesManager> findServicesManager(final Class clazz) {
        return serviceManagers.stream().filter(s -> s.supports(clazz)).findFirst();
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        val manager = findServicesManager(registeredService);
        return manager.map(servicesManager -> servicesManager.save(registeredService)).orElse(null);
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService, final boolean publishEvent) {
        val manager = findServicesManager(registeredService);
        return manager.map(servicesManager -> servicesManager.save(registeredService, publishEvent)).orElse(null);
    }

    @Override
    public void deleteAll() {
        serviceManagers.forEach(ServicesManager::deleteAll);
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
        val manager = findServicesManager(svc);
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
        val manager = findServicesManager(service);
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
        val manager = findServicesManager(serviceId);
        return manager.map(servicesManager -> servicesManager.findServiceBy(serviceId, clazz)).orElse(null);
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final String serviceId, final Class<T> clazz) {
        val manager = findServicesManager(clazz);
        return manager.map(servicesManager -> servicesManager.findServiceBy(serviceId, clazz)).orElse(null);
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
            .collect(Collectors.toList());
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

    @Override
    public boolean supports(final Service service) {
        return findServicesManager(service).isPresent();
    }

    @Override
    public boolean supports(final RegisteredService service) {
        return findServicesManager(service).isPresent();
    }

    @Override
    public boolean supports(final Class clazz) {
        return findServicesManager(clazz).isPresent();
    }
}
