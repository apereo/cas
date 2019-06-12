package org.apereo.cas.adaptors.cassandra.services;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.model.support.cassandra.serviceregistry.CassandraServiceRegistryProperties;
import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.serialization.StringSerializer;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link CassandraServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@ToString
public class CassandraServiceRegistry extends AbstractServiceRegistry implements DisposableBean {
    private static final StringSerializer<RegisteredService> SERIALIZER = new RegisteredServiceJsonSerializer();

    private final Mapper<CassandraRegisteredServiceHolder> entityManager;
    private final Session cassandraSession;
    private final CassandraServiceRegistryProperties properties;

    public CassandraServiceRegistry(final CassandraSessionFactory cassandraSessionFactory,
                                    final CassandraServiceRegistryProperties properties,
                                    final ApplicationEventPublisher eventPublisher,
                                    final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(eventPublisher, serviceRegistryListeners);
        this.properties = properties;
        this.cassandraSession = cassandraSessionFactory.getSession();
        val mappingManager = new MappingManager(this.cassandraSession);
        this.entityManager = mappingManager.mapper(CassandraRegisteredServiceHolder.class);
    }

    @Override
    public RegisteredService save(final RegisteredService rs) {
        try {
            val data = SERIALIZER.toString(rs);
            invokeServiceRegistryListenerPreSave(rs);
            entityManager.save(new CassandraRegisteredServiceHolder(rs.getId(), data), getConsistencyLevel());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return rs;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        try {
            entityManager.delete(registeredService.getId(), getConsistencyLevel());
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public long size() {
        try {
            val query = String.format("SELECT COUNT(*) FROM %s", CassandraRegisteredServiceHolder.TABLE_NAME);
            return cassandraSession.execute(query).one().getLong(0);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public Collection<RegisteredService> load() {
        try {
            val query = String.format("SELECT id, data FROM %s", CassandraRegisteredServiceHolder.TABLE_NAME);
            val results = cassandraSession.execute(query);
            val mappedResults = entityManager.map(results);
            return mappedResults
                .all()
                .stream()
                .map(holder -> SERIALIZER.from(holder.getData()))
                .filter(Objects::nonNull)
                .map(this::invokeServiceRegistryListenerPostLoad)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            val holder = entityManager.get(id);
            if (holder != null) {
                return SERIALIZER.from(holder.getData());
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

    @Override
    public void destroy() {
        this.cassandraSession.close();
    }

    private Mapper.Option getConsistencyLevel() {
        return Mapper.Option.consistencyLevel(ConsistencyLevel.valueOf(properties.getConsistencyLevel()));
    }

}
