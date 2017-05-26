package org.apereo.cas.services;

import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.View;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.google.common.base.Throwables;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This is {@link CouchbaseServiceRegistryDao}.
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
public class CouchbaseServiceRegistryDao extends AbstractServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CouchbaseServiceRegistryDao.class);

    private static final View ALL_SERVICES_VIEW = DefaultView.create(
            "all_services",
            "function(d,m) {if (!isNaN(m.id)) {emit(m.id);}}");

    private static final List<View> ALL_VIEWS = Arrays.asList(new View[]{ALL_SERVICES_VIEW});

    private static final String UTIL_DOCUMENT = "utils";

    private final CouchbaseClientFactory couchbase;
    private final StringSerializer<RegisteredService> registeredServiceJsonSerializer;

    /**
     * Default constructor.
     *
     * @param couchbase             couchbase instance
     * @param serviceJsonSerializer the JSON serializer to use.
     * @param isQueryEnabled        the is query enabled
     */
    public CouchbaseServiceRegistryDao(final CouchbaseClientFactory couchbase, final StringSerializer<RegisteredService> serviceJsonSerializer,
                                       final boolean isQueryEnabled) {
        this.couchbase = couchbase;
        this.registeredServiceJsonSerializer = serviceJsonSerializer;

        System.setProperty("com.couchbase.queryEnabled", Boolean.toString(isQueryEnabled));
        this.couchbase.ensureIndexes(UTIL_DOCUMENT, ALL_VIEWS);
        this.couchbase.initialize();
    }

    @Override
    public RegisteredService save(final RegisteredService service) {
        LOGGER.debug("Saving service [{}]", service);

        if (service.getId() == AbstractRegisteredService.INITIAL_IDENTIFIER_VALUE) {
            ((AbstractRegisteredService) service).setId(service.hashCode());
        }

        final StringWriter stringWriter = new StringWriter();
        this.registeredServiceJsonSerializer.to(stringWriter, service);

        this.couchbase.bucket().upsert(
                RawJsonDocument.create(
                        String.valueOf(service.getId()),
                        0, stringWriter.toString()));
        return service;
    }

    @Override
    public boolean delete(final RegisteredService service) {
        LOGGER.debug("Deleting service [{}]", service);
        this.couchbase.bucket().remove(String.valueOf(service.getId()));
        return true;
    }

    @Override
    public List<RegisteredService> load() {
        try {
            LOGGER.debug("Loading services");
            final ViewResult allKeys = executeViewQueryForAllServices();
            final List<RegisteredService> services = new LinkedList<>();
            for (final ViewRow row : allKeys) {

                final RawJsonDocument document = row.document(RawJsonDocument.class);
                if (document != null) {
                    final String json = document.content();
                    LOGGER.debug("Found service: [{}]", json);

                    final StringReader stringReader = new StringReader(json);
                    final RegisteredService service = this.registeredServiceJsonSerializer.from(stringReader);
                    services.add(service);
                    publishEvent(new CasRegisteredServiceLoadedEvent(this, service));
                }
            }
            return services;
        } catch (final RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            return new LinkedList<>();
        }
    }

    private ViewResult executeViewQueryForAllServices() {
        return this.couchbase.bucket().query(ViewQuery.from(UTIL_DOCUMENT, ALL_SERVICES_VIEW.name()));
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            LOGGER.debug("Lookup for service [{}]", id);
            final RawJsonDocument document = this.couchbase.bucket().get(String.valueOf(id), RawJsonDocument.class);
            if (document != null) {
                final String json = document.content();
                final StringReader stringReader = new StringReader(json);
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
    @PreDestroy
    public void destroy() {
        try {
            this.couchbase.shutdown();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public long size() {
        return executeViewQueryForAllServices().totalRows();
    }
}
