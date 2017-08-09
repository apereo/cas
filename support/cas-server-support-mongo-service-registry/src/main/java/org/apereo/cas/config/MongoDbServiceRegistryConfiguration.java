package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.serviceregistry.MongoServiceRegistryProperties;
import org.apereo.cas.mongo.MongoDbObjectFactory;
import org.apereo.cas.services.MongoServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("mongoDbServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbServiceRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "mongoDbServiceRegistryTemplate")
    @Bean
    public MongoTemplate mongoDbServiceRegistryTemplate() {
        final MongoDbObjectFactory factory = new MongoDbObjectFactory();
        return factory.buildMongoTemplate(casProperties.getServiceRegistry().getMongo());
    }
    
    @Bean
    public ServiceRegistryDao serviceRegistryDao() throws Exception {
        final MongoServiceRegistryProperties mongo = casProperties.getServiceRegistry().getMongo();
        return new MongoServiceRegistryDao(
                mongoDbServiceRegistryTemplate(),
                mongo.getCollectionName(),
                mongo.isDropCollection());
    }
}
