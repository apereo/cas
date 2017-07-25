package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.serviceregistry.MongoServiceRegistryProperties;
import org.apereo.cas.mongo.MongoDbObjectFactory;
import org.apereo.cas.services.MongoServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.convert.BaseConverters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import java.util.Arrays;

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
        factory.setCustomConversions(customConversions());
        return factory.buildMongoTemplate(casProperties.getServiceRegistry().getMongo());
    }
    
    private CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(
                new BaseConverters.LoggerConverter(),
                new BaseConverters.ClassConverter(),
                new BaseConverters.CommonsLogConverter(),
                new BaseConverters.PersonAttributesConverter(),
                new BaseConverters.CacheLoaderConverter(),
                new BaseConverters.RunnableConverter(),
                new BaseConverters.ReferenceQueueConverter(),
                new BaseConverters.ThreadLocalConverter(),
                new BaseConverters.CertPathConverter(),
                new BaseConverters.CacheConverter()
        ));
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
