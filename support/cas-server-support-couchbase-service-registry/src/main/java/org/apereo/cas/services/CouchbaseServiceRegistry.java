package org.apereo.cas.services;

import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.couchbase.core.CouchbaseException;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.serialization.StringSerializer;

import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.dsl.Expression;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link CouchbaseServiceRegistry}.
 * A Service Registry storage backend which uses the memcached protocol.
 * This may seem like a weird idea until you realize that CouchBase is a
 * multi host NoSQL database with a memcached interface to persistent
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

    public CouchbaseServiceRegistry(final ApplicationEventPublisher eventPublisher, final CouchbaseClientFactory couchbase,
                                    final StringSerializer<RegisteredService> registeredServiceJsonSerializer,
                                    final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(eventPublisher, serviceRegistryListeners);
        this.couchbase = couchbase;
        this.registeredServiceJsonSerializer = registeredServiceJsonSerializer;
    }

    @Override
    public RegisteredService save(final RegisteredService service) {
        LOGGER.trace("Saving service [{}] [{}]", service.getClass().getName(), service.getName());
        if (service.getId() == AbstractRegisteredService.INITIAL_IDENTIFIER_VALUE) {
            service.setId(UUID.randomUUID().getLeastSignificantBits());
        }
        val stringWriter = new StringWriter();
        this.registeredServiceJsonSerializer.to(stringWriter, service);
        val document = RawJsonDocument.create(String.valueOf(service.getId()), 0, stringWriter.toString());
        invokeServiceRegistryListenerPreSave(service);
        val savedDocument= couchbase.getBucket().upsert(document, couchbase.getTimeout(), TimeUnit.MILLISECONDS);
        val savedService = registeredServiceJsonSerializer.from(savedDocument.content());
        LOGGER.debug("Saved service [{}] as [{}]", service.getName(), savedService.getName());
        publishEvent(new CouchbaseRegisteredServiceSavedEvent(this));
        return savedService;
    }

    @Override
    public boolean delete(final RegisteredService service) {
        LOGGER.debug("Deleting service [{}]", service.getName());
        this.couchbase.getBucket().remove(String.valueOf(service.getId()), couchbase.getTimeout(), TimeUnit.MILLISECONDS);
        publishEvent(new CouchbaseRegisteredServiceDeletedEvent(this));
        return true;
    }

    @Override
    public Collection<RegisteredService> load() {
        try {
            val allKeys = executeViewQueryForAllServices();
            val bucketName = couchbase.getBucket().name();
            val spliterator = Spliterators.spliteratorUnknownSize(allKeys.iterator(), Spliterator.ORDERED);
            return StreamSupport.stream(spliterator, false)
                .map(N1qlQueryRow::value)
                .filter(Objects::nonNull)
                .filter(document -> document.containsKey(bucketName))
                .map(document -> {
                    val json = document.getObject(bucketName).toString();
                    LOGGER.trace("Found service: [{}]", json);
                    return this.registeredServiceJsonSerializer.from(json);
                })
                .filter(Objects::nonNull)
                .map(this::invokeServiceRegistryListenerPostLoad)
                .filter(Objects::nonNull)
                .peek(service -> publishEvent(new CasRegisteredServiceLoadedEvent(this, service)))
                .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private N1qlQueryResult executeViewQueryForAllServices() {
        val theBucket = couchbase.getBucket();
        val statement = Select.select("*")
            .from(Expression.i(theBucket.name()))
            .where("REGEX_CONTAINS(" + Expression.i("@class") + ", \".*RegisteredService$\")");

        val n1q1Query = N1qlQuery.simple(statement);
        val queryResult = theBucket.query(n1q1Query, couchbase.getTimeout(), TimeUnit.MILLISECONDS);
        if (!queryResult.finalSuccess()) {
            throw new CouchbaseException(queryResult.status() + ": " + queryResult.errors().toString());
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("executeViewQueryForAllServices() [");
            queryResult.allRows().forEach(r -> LOGGER.trace("[{}]", r));
            LOGGER.trace("]");
        }
        return queryResult;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            LOGGER.debug("Lookup for service long: [{}]", id);
            val document = couchbase.getBucket().get(String.valueOf(id), RawJsonDocument.class);
            if (document != null) {
                val json = document.content();
                val stringReader = new StringReader(json);
                return registeredServiceJsonSerializer.from(stringReader);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        LOGGER.debug("Lookup for service string: [{}]", id);
        return load().stream().filter(r -> r.matches(id)).findFirst().orElse(null);
    }

    /**
     * Stops the couchbase client and cancels the initialization task if uncompleted.
     */
    @SneakyThrows
    @Override
    public void destroy() {
        this.couchbase.shutdown();
    }

    @Override
    public long size() {
        return executeViewQueryForAllServices().allRows().size();
    }
}
