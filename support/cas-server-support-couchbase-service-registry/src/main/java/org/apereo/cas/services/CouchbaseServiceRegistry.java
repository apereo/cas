package org.apereo.cas.services;

import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.serialization.StringSerializer;

import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.dsl.Expression;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
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
@RequiredArgsConstructor
public class CouchbaseServiceRegistry extends AbstractServiceRegistry implements DisposableBean {
    private final CouchbaseClientFactory couchbase;

    private final StringSerializer<RegisteredService> registeredServiceJsonSerializer;

    @Override
    @SneakyThrows
    public RegisteredService save(final RegisteredService service) {
        LOGGER.trace("Saving service [{}]", service.getName());
        if (service.getId() == AbstractRegisteredService.INITIAL_IDENTIFIER_VALUE) {
            service.setId(service.hashCode());
        }
        try (val stringWriter = new StringWriter()) {
            this.registeredServiceJsonSerializer.to(stringWriter, service);
            val document = RawJsonDocument.create(String.valueOf(service.getId()), 0, stringWriter.toString());
            couchbase.getBucket().upsert(document, couchbase.getTimeout(), TimeUnit.MILLISECONDS);
            LOGGER.debug("Saved service [{}]", service.getName());
            publishEvent(new CouchbaseRegisteredServiceSavedEvent(this));
        }
        return service;
    }

    @Override
    public boolean delete(final RegisteredService service) {
        LOGGER.debug("Deleting service [{}]", service.getName());
        this.couchbase.getBucket().remove(String.valueOf(service.getId()));
        publishEvent(new CouchbaseRegisteredServiceDeletedEvent(this));
        return true;
    }

    @Override
    public Collection<RegisteredService> load() {
        try {
            val allKeys = executeViewQueryForAllServices();
            val bucketName = couchbase.getBucket().name();
            if (allKeys == null) {
                LOGGER.info("No registered services could be found in bucket [{}]", bucketName);
                return new ArrayList<>(0);
            }
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
            .where(Expression.i("@class").like('"' + RegisteredService.class.getPackageName().concat("%") + '"'));

        val n1q1Query = N1qlQuery.simple(statement);
        return theBucket.query(n1q1Query, couchbase.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            LOGGER.debug("Lookup for service [{}]", id);
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
