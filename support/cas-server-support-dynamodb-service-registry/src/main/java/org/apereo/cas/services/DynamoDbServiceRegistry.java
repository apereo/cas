package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link DynamoDbServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DynamoDbServiceRegistry extends AbstractServiceRegistry {
    private final DynamoDbServiceRegistryFacilitator dbTableService;

    public DynamoDbServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                   final DynamoDbServiceRegistryFacilitator dbTableService,
                                   final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.dbTableService = dbTableService;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return FunctionUtils.doUnchecked(() -> {
            registeredService.assignIdIfNecessary();
            invokeServiceRegistryListenerPreSave(registeredService);
            dbTableService.put(registeredService);
            return registeredService;
        });
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return dbTableService.delete(registeredService);
    }

    @Override
    public void deleteAll() {
        this.dbTableService.deleteAll();
    }

    @Override
    public Collection<RegisteredService> load() {
        val svc = dbTableService.getAll();
        val clientInfo = ClientInfoHolder.getClientInfo();
        return svc
            .stream()
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .peek(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s, clientInfo)))
            .collect(Collectors.toList());
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return dbTableService.get(id);
    }

    @Override
    public long size() {
        return dbTableService.count();
    }
}
