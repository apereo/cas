package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.couchbase.serviceregistry.CouchbaseServiceRegistryProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.CouchbaseServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * This is {@link CouchbaseServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("couchbaseServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchbaseServiceRegistryConfiguration {
    
    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Service registry couchbase client factory couchbase client factory.
     *
     * @return the couchbase client factory
     */
    @RefreshScope
    @Bean
    public CouchbaseClientFactory serviceRegistryCouchbaseClientFactory() {
        final CouchbaseServiceRegistryProperties couchbase = casProperties.getServiceRegistry().getCouchbase();
        final Set<String> nodes = StringUtils.commaDelimitedListToSet(couchbase.getNodeSet());
        return new CouchbaseClientFactory(nodes, couchbase.getBucket(),
                couchbase.getPassword(), couchbase.getTimeout(),
                CouchbaseServiceRegistryDao.UTIL_DOCUMENT,
                CouchbaseServiceRegistryDao.ALL_VIEWS);
    }

    @Bean
    @RefreshScope
    public ServiceRegistryDao serviceRegistryDao() {
        return new CouchbaseServiceRegistryDao(serviceRegistryCouchbaseClientFactory(), new DefaultRegisteredServiceJsonSerializer(),
                casProperties.getServiceRegistry().getCouchbase().isQueryEnabled());
    }
}
