package org.apereo.cas.services.cassandra;


import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.model.support.cassandra.serviceregistry.CassandraServiceRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.query.Query;

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
    private final StringSerializer<RegisteredService> serializer;

    private final CassandraSessionFactory cassandraSessionFactory;

    private final CassandraServiceRegistryProperties properties;

    public CassandraServiceRegistry(final CassandraSessionFactory cassandraSessionFactory,
                                    final CassandraServiceRegistryProperties properties,
                                    final ConfigurableApplicationContext applicationContext,
                                    final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.properties = properties;
        this.cassandraSessionFactory = cassandraSessionFactory;
        this.serializer = new RegisteredServiceJsonSerializer(applicationContext);
    }

    @Override
    public RegisteredService save(final RegisteredService rs) {
        try {
            rs.assignIdIfNecessary();
            val data = serializer.toString(rs);
            invokeServiceRegistryListenerPreSave(rs);
            val options = InsertOptions.builder()
                .consistencyLevel(DefaultConsistencyLevel.valueOf(properties.getConsistencyLevel()))
                .serialConsistencyLevel(DefaultConsistencyLevel.valueOf(properties.getSerialConsistencyLevel()))
                .timeout(Beans.newDuration(properties.getTimeout()))
                .build();
            val result = cassandraSessionFactory.getCassandraTemplate()
                .insert(new CassandraRegisteredServiceHolder(rs.getId(), data), options);
            return serializer.from(result.getEntity().getData());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
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
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public void deleteAll() {
        cassandraSessionFactory.getCassandraTemplate().truncate(CassandraRegisteredServiceHolder.class);
    }

    @Override
    public long size() {
        return cassandraSessionFactory.getCassandraTemplate()
            .count(CassandraRegisteredServiceHolder.class);
    }

    @Override
    public Collection<RegisteredService> load() {
        val results = cassandraSessionFactory.getCassandraTemplate().select(Query.query(), CassandraRegisteredServiceHolder.class);
        return results.stream()
            .map(holder -> serializer.from(holder.getData()))
            .filter(Objects::nonNull)
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        val holder = cassandraSessionFactory.getCassandraTemplate()
            .selectOneById(id, CassandraRegisteredServiceHolder.class);
        if (holder != null) {
            return serializer.from(holder.getData());
        }
        return null;
    }

    @Override
    public void destroy() throws Exception {
        this.cassandraSessionFactory.close();
    }

}
