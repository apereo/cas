package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;

import lombok.ToString;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Default In Memory Service Registry Dao for test/demonstration purposes.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@ToString
public class InMemoryServiceRegistry extends AbstractServiceRegistry {

    private final List<RegisteredService> registeredServices;

    public InMemoryServiceRegistry(final ConfigurableApplicationContext applicationContext) {
        this(applicationContext, new ArrayList<>(0), new ArrayList<>(0));
    }

    public InMemoryServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                   final List<RegisteredService> registeredServices,
                                   final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.registeredServices = registeredServices;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return this.registeredServices.remove(registeredService);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return this.registeredServices.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    @Override
    public Collection<RegisteredService> load() {
        val services = new ArrayList<RegisteredService>(registeredServices.size());
        registeredServices
            .stream()
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .forEach(s -> {
                publishEvent(new CasRegisteredServiceLoadedEvent(this, s));
                services.add(s);
            });
        return services;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        if (registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            registeredService.setId(findHighestId() + 1);
        }
        invokeServiceRegistryListenerPreSave(registeredService);
        val svc = findServiceById(registeredService.getId());
        if (svc != null) {
            this.registeredServices.remove(svc);
        }
        this.registeredServices.add(registeredService);
        return registeredService;
    }

    /**
     * This isn't super-fast but we don't expect thousands of services.
     *
     * @return the highest service id in the list of registered services
     */
    private long findHighestId() {
        return this.registeredServices.stream().map(RegisteredService::getId).max(Comparator.naturalOrder()).orElse(0L);
    }

    @Override
    public long size() {
        return registeredServices.size();
    }

    @Override
    public Stream<? extends RegisteredService> getServicesStream() {
        return this.registeredServices.stream();
    }
}
