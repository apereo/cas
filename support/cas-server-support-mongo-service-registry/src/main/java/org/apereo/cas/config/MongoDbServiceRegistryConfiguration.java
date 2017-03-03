package org.apereo.cas.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClientOptions;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.serviceregistry.MongoServiceRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.MongoServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.convert.BaseConverters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
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
public class MongoDbServiceRegistryConfiguration extends AbstractMongoConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Override
    protected String getDatabaseName() {
        final MongoServiceRegistryProperties mongo = casProperties.getServiceRegistry().getMongo();
        return mongo.getDatabaseName();
    }

    @Override
    public Mongo mongo() throws Exception {
        final MongoServiceRegistryProperties mongo = casProperties.getServiceRegistry().getMongo();
        return Beans.newMongoDbClient(mongo);
    }
    
    @RefreshScope
    @Bean
    public MongoClientOptions mongoClientOptions() throws Exception {
        final MongoServiceRegistryProperties mongo = casProperties.getServiceRegistry().getMongo();
        return Beans.newMongoDbClientOptions(mongo);
    }

    @Override
    public CustomConversions customConversions() {
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
                mongoTemplate(),
                mongo.getCollectionName(),
                mongo.isDropCollection());
    }
}
