package org.apereo.cas.services;

import org.apereo.cas.couchdb.services.RegisteredServiceCouchDbRepository;
import org.apereo.cas.couchdb.services.RegisteredServiceDocument;
import org.apereo.cas.util.RandomUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link CouchDbServiceRegistry}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Slf4j
public class CouchDbServiceRegistry extends AbstractServiceRegistry {

    private final RegisteredServiceCouchDbRepository dbClient;

    public CouchDbServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                  final RegisteredServiceCouchDbRepository dbClient,
                                  final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.dbClient = dbClient;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        LOGGER.debug("Saving service [{}]", registeredService.getName());
        if (registeredService.getId() < 0) {
            registeredService.setId(RandomUtils.nextLong());
        }
        try {
            val svc = dbClient.get(registeredService.getId());
            invokeServiceRegistryListenerPreSave(registeredService);
            if (svc != null) {
                val doc = new RegisteredServiceDocument(registeredService);
                doc.setRevision(svc.getRevision());
                dbClient.update(doc);
                LOGGER.debug("Service [{}] with id [{}] updated", registeredService.getName(), registeredService.getId());
            } else {
                dbClient.add(new RegisteredServiceDocument(registeredService));
                LOGGER.debug("New service [{}] with id [{}] created", registeredService.getName(), registeredService.getId());
            }
        } catch (final DbAccessException e) {
            LOGGER.debug("Failed to update service [{}] with id [{}]: [{}]", registeredService.getName(), registeredService.getId(), e.getMessage());
            return null;
        }
        return registeredService;
    }

    @Override
    public boolean delete(final RegisteredService service) {
        LOGGER.debug("Deleting service [{}]", service.getName());

        try {
            dbClient.deleteRecord(new RegisteredServiceDocument(service));
            LOGGER.debug("Successfully deleted service [{}] with id [{}].", service.getName(), service.getId());
            return true;
        } catch (final DbAccessException exception) {
            LOGGER.debug("Could not delete service [{}] with id [{}]: [{}]", service.getName(), service.getId(), exception.getMessage());
            return false;
        }
    }

    @Override
    public Collection<RegisteredService> load() {
        return dbClient.getAll()
            .stream()
            .map(RegisteredServiceDocument::getService)
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            val doc = dbClient.get(id);
            if (doc != null) {
                return doc.getService();
            }
        } catch (final DocumentNotFoundException e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    @Override
    public RegisteredService findServiceByExactServiceId(final String id) {
        val doc = dbClient.findByServiceId(id);
        if (doc == null) {
            return null;
        }
        return doc.getService();
    }

    @Override
    public RegisteredService findServiceByExactServiceName(final String name) {
        val doc = dbClient.findByServiceName(name);
        if (doc == null) {
            return null;
        }
        return doc.getService();
    }

    @Override
    public long size() {
        return dbClient.size();
    }
}
