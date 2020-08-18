package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link ChainingServicesManager}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
public class ChainingServicesManager implements ServicesManager, DomainAwareServicesManager {

    private final List<ServicesManager> serviceManagers = new ArrayList<>();

    /**
     * Adds a services manager to the chain.
     *
     * @param manager - a services manager
     */
    public void registerServiceManager(final ServicesManager manager) {
        this.serviceManagers.add(manager);
        AnnotationAwareOrderComparator.sortIfNecessary(serviceManagers);
    }

    @Audit(action = "SAVE_SERVICE",
        actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER",
        resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        val manager = findServicesManager(registeredService);
        return manager.map(servicesManager -> servicesManager.save(registeredService)).orElse(null);
    }

    @Audit(action = "SAVE_SERVICE",
        actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER",
        resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public RegisteredService save(final RegisteredService registeredService, final boolean publishEvent) {
        val manager = findServicesManager(registeredService);
        return manager.map(servicesManager -> servicesManager.save(registeredService, publishEvent)).orElse(null);
    }

    @Override
    public void deleteAll() {
        serviceManagers.forEach(ServicesManager::deleteAll);
    }

    @Audit(action = "DELETE_SERVICE",
        actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER",
        resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public RegisteredService delete(final long id) {
        return serviceManagers.stream()
            .map(s -> s.delete(id))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Audit(action = "DELETE_SERVICE",
        actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER",
        resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public RegisteredService delete(final RegisteredService svc) {
        val manager = findServicesManager(svc);
        return manager.map(servicesManager -> servicesManager.delete(svc)).orElse(null);
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
        return manager.map(servicesManager -> servicesManager.findServiceBy(service)).orElse(null);
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
    public <T extends RegisteredService> T findServiceBy(final long id, final Class<T> clazz) {
        val manager = findServicesManager(clazz);
        return manager.map(servicesManager -> servicesManager.findServiceBy(id, clazz)).orElse(null);
    }

    @Override
    public RegisteredService findServiceByName(final String name) {
        return serviceManagers.stream()
            .map(s -> s.findServiceByName(name))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public <T extends RegisteredService> T findServiceByName(final String name, final Class<T> clazz) {
        val manager = findServicesManager(clazz);
        return manager.map(servicesManager -> servicesManager.findServiceByName(name, clazz)).orElse(null);
    }

    @Override
    public RegisteredService findServiceByExactServiceId(final String serviceId) {
        return serviceManagers.stream()
            .map(s -> s.findServiceByExactServiceId(serviceId))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public Stream<String> getDomains() {
        return serviceManagers.stream()
            .filter(mgr -> mgr instanceof DomainAwareServicesManager)
            .map(DomainAwareServicesManager.class::cast)
            .flatMap(DomainAwareServicesManager::getDomains);
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
            .filter(mgr -> mgr instanceof DomainAwareServicesManager)
            .map(DomainAwareServicesManager.class::cast)
            .flatMap(d -> d.getServicesForDomain(domain).stream())
            .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return serviceManagers.stream()
            .mapToLong(ServicesManager::count)
            .sum();
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

    private Optional<ServicesManager> findServicesManager(final RegisteredService service) {
        return serviceManagers.stream().filter(s -> s.supports(service)).findFirst();
    }

    private Optional<ServicesManager> findServicesManager(final Service service) {
        return serviceManagers.stream().filter(s -> s.supports(service)).findFirst();
    }

    private Optional<ServicesManager> findServicesManager(final Class<?> clazz) {
        return serviceManagers.stream().filter(s -> s.supports(clazz)).findFirst();
    }

}
