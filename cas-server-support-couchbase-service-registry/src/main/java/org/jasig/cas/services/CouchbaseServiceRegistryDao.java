package org.jasig.cas.services;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.View;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import org.jasig.cas.couchbase.core.CouchbaseClientFactory;
import org.jasig.cas.util.JsonSerializer;
import org.jasig.cas.util.services.RegisteredServiceJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
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
@Component("couchbaseServiceRegistryDao")
public class CouchbaseServiceRegistryDao implements ServiceRegistryDao {
    private static final View ALL_SERVICES_VIEW = DefaultView.create(
            "all_services",
            "function(d,m) {if (!isNaN(m.id)) {emit(m.id);}}");

    private static final List<View> ALL_VIEWS = Arrays.asList(new View[]{
            ALL_SERVICES_VIEW
    });

    private static final String UTIL_DOCUMENT = "utils";

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    @Autowired
    @Qualifier("serviceRegistryCouchbaseClientFactory")
    private CouchbaseClientFactory couchbase;

    @Value("${svcreg.couchbase.query.enabled:true}")
    private boolean queryEnabled;

    private final JsonSerializer<RegisteredService> registeredServiceJsonSerializer;

    /**
     * Default constructor.
     *
     * @param serviceJsonSerializer the JSON serializer to use.
     */
    public CouchbaseServiceRegistryDao(final JsonSerializer<RegisteredService> serviceJsonSerializer) {
        this.registeredServiceJsonSerializer = serviceJsonSerializer;
    }

    /**
     * Default constructor.
     */
    public CouchbaseServiceRegistryDao() {
        this(new RegisteredServiceJsonSerializer());
    }

    @Override
    public RegisteredService save(final RegisteredService service) {
        logger.debug("Saving service {}", service);

        if (service.getId() == AbstractRegisteredService.INITIAL_IDENTIFIER_VALUE) {
            ((AbstractRegisteredService) service).setId(service.hashCode());
        }

        final StringWriter stringWriter = new StringWriter();
        registeredServiceJsonSerializer.toJson(stringWriter, service);

        couchbase.bucket().upsert(
                RawJsonDocument.create(
                        String.valueOf(service.getId()),
                        0, stringWriter.toString()));
        return service;
    }

    @Override
    public boolean delete(final RegisteredService service) {
        logger.debug("Deleting service {}", service);
        couchbase.bucket().remove(String.valueOf(service.getId()));
        return true;
    }


    @Override
    public List<RegisteredService> load() {
        try {
            logger.debug("Loading services");

            final Bucket bucket = couchbase.bucket();
            final ViewResult allKeys = bucket.query(ViewQuery.from(UTIL_DOCUMENT, ALL_SERVICES_VIEW.name()));
            final List<RegisteredService> services = new LinkedList<>();
            for (final ViewRow row : allKeys) {

                final RawJsonDocument document = row.document(RawJsonDocument.class);
                if (document != null) {
                    final String json = document.content();
                    logger.debug("Found service: {}", json);

                    final StringReader stringReader = new StringReader(json);
                    services.add(registeredServiceJsonSerializer.fromJson(stringReader));
                }
            }
            return services;
        } catch (final RuntimeException e) {
            logger.error(e.getMessage(), e);
            return new LinkedList<>();
        }
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            logger.debug("Lookup for service {}", id);
            final RawJsonDocument document = couchbase.bucket().get(String.valueOf(id), RawJsonDocument.class);
            if (document != null) {
                final String json = document.content();
                final StringReader stringReader = new StringReader(json);
                return registeredServiceJsonSerializer.fromJson(stringReader);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Starts the couchbase client and initialization task.
     */
    @PostConstruct
    public void initialize() {
        System.setProperty("com.couchbase.queryEnabled", Boolean.toString(this.queryEnabled));
        couchbase.ensureIndexes(UTIL_DOCUMENT, ALL_VIEWS);
        couchbase.initialize();
    }

    /**
     * Stops the couchbase client and cancels the initialization task if uncompleted.
     */
    @PreDestroy
    public void destroy() {
        try {
            couchbase.shutdown();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setCouchbaseClientFactory(final CouchbaseClientFactory couchbase) {
        this.couchbase = couchbase;
    }
}
