package org.apereo.cas.services;

import org.apereo.cas.couchdb.services.RegisteredServiceDocument;
import org.apereo.cas.couchdb.services.RegisteredServiceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.UpdateConflictException;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link CouchDbServiceRegistry}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class CouchDbServiceRegistry extends AbstractServiceRegistry {

    private final RegisteredServiceRepository dbClient;
    private final int conflictRetries;

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        LOGGER.debug("Saving service [{}]", registeredService.getName());
        if (registeredService.getId() < 0) {
            registeredService.setId(registeredService.hashCode());
        }
        dbClient.add(new RegisteredServiceDocument(registeredService));
        return registeredService;
    }

    @Override
    public boolean delete(final RegisteredService service) {
        LOGGER.debug("Deleting service [{}]", service.getName());
        var exception = (UpdateConflictException) null;
        var success = false;
        for (var retries = 0; retries < conflictRetries; retries++) {
            try {
                exception = null;
                val serviceDocument = dbClient.get(service.getId());
                dbClient.remove(serviceDocument);
                success = true;
            } catch (final UpdateConflictException e) {
                exception = e;
            }
            if (success) {
                LOGGER.debug("Successfully deleted service [{}].", service.getName());
                return false;
            }
        }
        if (exception != null) {
            LOGGER.debug("Could not delete service [{}] {}", service.getName(), exception.getMessage());
        }
        return false;
    }

    @Override
    public Collection<RegisteredService> load() {
        return dbClient.getAll().stream().map(RegisteredServiceDocument::getService).collect(Collectors.toList());
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return dbClient.get(id).getService();
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        return dbClient.get(id).getService();
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
