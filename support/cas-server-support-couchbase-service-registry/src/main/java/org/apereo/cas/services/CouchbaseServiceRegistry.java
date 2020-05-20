package org.apereo.cas.services;

import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.couchbase.core.CouchbaseException;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.serialization.StringSerializer;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link CouchbaseServiceRegistry}.
 * A Service Registry storage backend which uses the memcached protocol.
 * CouchBase is a multi host NoSQL database with a memcached interface to persistent
 * storage which also is quite usable as a replicated ticket storage
 * engine for multiple front end CAS servers.
 *
 * @author Fredrik Jönsson "fjo@kth.se"
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
public class CouchbaseServiceRegistry extends AbstractServiceRegistry implements DisposableBean {
    private final CouchbaseClientFactory couchbase;

    private final StringSerializer<RegisteredService> registeredServiceJsonSerializer;

    public CouchbaseServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                    final CouchbaseClientFactory couchbase,
                                    final StringSerializer<RegisteredService> registeredServiceJsonSerializer,
                                    final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.couchbase = couchbase;
        this.registeredServiceJsonSerializer = registeredServiceJsonSerializer;
    }

    @Override
    public RegisteredService save(final RegisteredService service) {
        LOGGER.trace("Saving service [{}]:[{}]", service.getClass().getName(), service.getName());
        if (service.getId() == AbstractRegisteredService.INITIAL_IDENTIFIER_VALUE) {
            service.setId(UUID.randomUUID().getLeastSignificantBits());
        }
        val stringWriter = new StringWriter();
        this.registeredServiceJsonSerializer.to(stringWriter, service);
        invokeServiceRegistryListenerPreSave(service);
        couchbase.bucketUpsertDefaultCollection(String.valueOf(service.getId()), stringWriter.toString());
        LOGGER.debug("Saved service [{}] as [{}]", service.getName(), service.getName());
        publishEvent(new CouchbaseRegisteredServiceSavedEvent(this));
        return service;
    }

    @Override
    public boolean delete(final RegisteredService service) {
        LOGGER.trace("Deleting service [{}]", service.getName());
        this.couchbase.bucketRemoveFromDefaultCollection(String.valueOf(service.getId()));
        publishEvent(new CouchbaseRegisteredServiceDeletedEvent(this));
        return true;
    }

    @Override
    public Collection<RegisteredService> load() {
        try {
            val allServices = queryForAllServices().rowsAsObject();
            val spliterator = Spliterators.spliteratorUnknownSize(allServices.iterator(), Spliterator.ORDERED);
            return StreamSupport.stream(spliterator, false)
                .filter(document -> document.containsKey(couchbase.getBucket()))
                .map(document -> {
                    val json = document.getString(couchbase.getBucket());
                    LOGGER.trace("Found service: [{}]", json);
                    return this.registeredServiceJsonSerializer.from(json);
                })
                .filter(Objects::nonNull)
                .map(this::invokeServiceRegistryListenerPostLoad)
                .filter(Objects::nonNull)
                .peek(service -> publishEvent(new CasRegisteredServiceLoadedEvent(this, service)))
                .collect(Collectors.toList());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            val document = couchbase.bucketGet(String.valueOf(id));
            if (document != null) {
                val json = document.contentAs(String.class);
                try (val stringReader = new StringReader(json)) {
                    return registeredServiceJsonSerializer.from(stringReader);
                }
            }
        } catch (final DocumentNotFoundException e) {
            LOGGER.debug(e.getMessage(), e);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    @SneakyThrows
    @Override
    public void destroy() {
        this.couchbase.shutdown();
    }

    @Override
    public long size() {
        return queryForAllServices().rowsAsObject().size();
    }

    private QueryResult queryForAllServices() {
        val query = String.format("REGEX_CONTAINS(%s, \"@class.*:.*RegisteredService\")", couchbase.getBucket());
        val queryResult = couchbase.select(query);
        if (!queryResult.metaData().status().equals(QueryStatus.SUCCESS)) {
            throw new CouchbaseException(queryResult.metaData().toString());
        }
        return queryResult;
    }
}
