package org.apereo.cas.services.cassandra;


import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.model.support.cassandra.serviceregistry.CassandraServiceRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.serialization.StringSerializer;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.query.Query;

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
    private static final StringSerializer<RegisteredService> SERIALIZER =
        new RegisteredServiceJsonSerializer(new MinimalPrettyPrinter());

    private final CassandraSessionFactory cassandraSessionFactory;

    private final CassandraServiceRegistryProperties properties;

    public CassandraServiceRegistry(final CassandraSessionFactory cassandraSessionFactory,
                                    final CassandraServiceRegistryProperties properties,
                                    final ConfigurableApplicationContext applicationContext,
                                    final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.properties = properties;
        this.cassandraSessionFactory = cassandraSessionFactory;
    }

    @Override
    public RegisteredService save(final RegisteredService rs) {
        try {
            val data = SERIALIZER.toString(rs);
            invokeServiceRegistryListenerPreSave(rs);
            val options = InsertOptions.builder()
                .consistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
                .serialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
                .timeout(Beans.newDuration(properties.getTimeout()))
                .build();
            val result = cassandraSessionFactory.getCassandraTemplate()
                .insert(new CassandraRegisteredServiceHolder(rs.getId(), data), options);
            return SERIALIZER.from(result.getEntity().getData());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return rs;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        try {
            
            cassandraSessionFactory.getCassandraTemplate()
                .deleteById(registeredService.getId(), CassandraRegisteredServiceHolder.class);
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public long size() {
        try {
            return cassandraSessionFactory.getCassandraTemplate()
                .count(CassandraRegisteredServiceHolder.class);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public Collection<RegisteredService> load() {
        try {
            val results = cassandraSessionFactory.getCassandraTemplate().select(Query.query(), CassandraRegisteredServiceHolder.class);
            return results.stream()
                .map(holder -> SERIALIZER.from(holder.getData()))
                .filter(Objects::nonNull)
                .map(this::invokeServiceRegistryListenerPostLoad)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            val holder = cassandraSessionFactory.getCassandraTemplate().selectOneById(id, CassandraRegisteredServiceHolder.class);
            if (holder != null) {
                return SERIALIZER.from(holder.getData());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void destroy() throws Exception {
        this.cassandraSessionFactory.close();
    }

}
