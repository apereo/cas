package org.apereo.cas.services;

import org.apereo.cas.couchdb.services.RegisteredServiceCouchDbRepository;
import org.apereo.cas.couchdb.services.RegisteredServiceDocument;
import org.apereo.cas.util.RandomUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;

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

    private final RegisteredServiceCouchDbRepository dbClient;

    private final int conflictRetries;

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        LOGGER.debug("Saving service [{}]", registeredService.getName());
        if (registeredService.getId() < 0) {
            registeredService.setId(RandomUtils.getNativeInstance().nextLong());
        }
        try {
            val svc = dbClient.get(registeredService.getId());
            val doc = new RegisteredServiceDocument(registeredService);
            doc.setRevision(svc.getRevision());
            dbClient.update(doc);
        } catch (final DocumentNotFoundException ignored) {
            LOGGER.debug("New service record created.");
            dbClient.add(new RegisteredServiceDocument(registeredService));
        } catch (final DbAccessException e) {
            LOGGER.debug("Failed to update service [{}] {}", registeredService.getName(), e.getMessage());
            return null;
        }
        return registeredService;
    }

    @Override
    public boolean delete(final RegisteredService service) {
        LOGGER.debug("Deleting service [{}]", service.getName());

        try {
            dbClient.deleteRecord(new RegisteredServiceDocument(service));
            LOGGER.debug("Successfully deleted service [{}].", service.getName());
            return true;
        } catch (final DbAccessException exception) {
            LOGGER.debug("Could not delete service [{}] {}", service.getName(), exception.getMessage());
            return false;
        }
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
