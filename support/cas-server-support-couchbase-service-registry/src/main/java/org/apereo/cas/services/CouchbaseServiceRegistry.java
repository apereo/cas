package org.apereo.cas.services;

import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.View;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

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

    /**
     * The utils document.
     */
    public static final String UTIL_DOCUMENT = "utils";

    /**
     * All services view.
     */
    public static final View ALL_SERVICES_VIEW = DefaultView.create(
        "all_services",
        "function(d,m) {if (!isNaN(m.id)) {emit(m.id);}}");

    /**
     * All views.
     */
    public static final Collection<View> ALL_VIEWS = CollectionUtils.wrap(ALL_SERVICES_VIEW);

    private final CouchbaseClientFactory couchbase;
    private final StringSerializer<RegisteredService> registeredServiceJsonSerializer;

    public CouchbaseServiceRegistry(final CouchbaseClientFactory couchbase,
                                    final StringSerializer<RegisteredService> serviceJsonSerializer) {
        this.couchbase = couchbase;
        this.registeredServiceJsonSerializer = serviceJsonSerializer;
    }

    @Override
    @SneakyThrows
    public RegisteredService save(final RegisteredService service) {
        LOGGER.debug("Saving service [{}]", service.getName());
        if (service.getId() == AbstractRegisteredService.INITIAL_IDENTIFIER_VALUE) {
            service.setId(service.hashCode());
        }
        try (val stringWriter = new StringWriter()) {
            this.registeredServiceJsonSerializer.to(stringWriter, service);
            val document = RawJsonDocument.create(String.valueOf(service.getId()), 0, stringWriter.toString());
            this.couchbase.getBucket().upsert(document);
        }
        return service;
    }

    @Override
    public boolean delete(final RegisteredService service) {
        LOGGER.debug("Deleting service [{}]", service.getName());
        this.couchbase.getBucket().remove(String.valueOf(service.getId()));
        return true;
    }

    @Override
    public Collection<RegisteredService> load() {
        try {
            val allKeys = executeViewQueryForAllServices();
            val services = new ArrayList<RegisteredService>();
            for (val row : allKeys) {
                val document = row.document(RawJsonDocument.class);
                if (document != null) {
                    val json = document.content();
                    LOGGER.debug("Found service: [{}]", json);

                    val stringReader = new StringReader(json);
                    val service = this.registeredServiceJsonSerializer.from(stringReader);
                    services.add(service);
                    publishEvent(new CasRegisteredServiceLoadedEvent(this, service));
                }
            }
            return services;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private ViewResult executeViewQueryForAllServices() {
        return this.couchbase.getBucket().query(ViewQuery.from(UTIL_DOCUMENT, ALL_SERVICES_VIEW.name()));
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            LOGGER.debug("Lookup for service [{}]", id);
            val document = this.couchbase.getBucket().get(String.valueOf(id), RawJsonDocument.class);
            if (document != null) {
                val json = document.content();
                val stringReader = new StringReader(json);
                return this.registeredServiceJsonSerializer.from(stringReader);
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
        return executeViewQueryForAllServices().totalRows();
    }
}
