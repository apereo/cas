package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import lombok.ToString;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.ArrayList;
import java.util.Collection;
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
        this(applicationContext, new ArrayList<>(), new ArrayList<>());
    }

    public InMemoryServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                   final List<RegisteredService> registeredServices,
                                   final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.registeredServices = registeredServices;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        registeredService.assignIdIfNecessary();
        invokeServiceRegistryListenerPreSave(registeredService);
        val svc = findServiceById(registeredService.getId());
        if (svc != null) {
            registeredServices.remove(svc);
        }
        registeredServices.add(registeredService);
        return registeredService;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return !registeredServices.contains(registeredService)
            || registeredServices.removeIf(rs -> rs.getId() == registeredService.getId());
    }

    @Override
    public void deleteAll() {
        this.registeredServices.clear();
    }

    @Override
    public Collection<RegisteredService> load() {
        val services = new ArrayList<RegisteredService>(registeredServices.size());
        val clientInfo = ClientInfoHolder.getClientInfo();
        registeredServices
            .stream()
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .forEach(s -> {
                publishEvent(new CasRegisteredServiceLoadedEvent(this, s, clientInfo));
                services.add(s);
            });
        return services;
    }

    @Override
    public Stream<? extends RegisteredService> getServicesStream() {
        return this.registeredServices.stream();
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return registeredServices.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    @Override
    public long size() {
        return registeredServices.size();
    }
}
