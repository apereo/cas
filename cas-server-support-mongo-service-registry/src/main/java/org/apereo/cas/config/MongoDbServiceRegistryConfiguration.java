package org.apereo.cas.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MongoServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.convert.BaseConverters;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoClientOptionsFactoryBean;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import java.util.Arrays;
import java.util.Collections;

/**
 * This is {@link MongoDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("mongoDbServiceRegistryConfiguration")
public class MongoDbServiceRegistryConfiguration extends AbstractMongoConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    /**
     * Persistence exception translation post processor persistence exception translation post processor.
     *
     * @return the persistence exception translation post processor
     */
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Override
    protected String getDatabaseName() {
        return casProperties.getMongoServiceRegistry().getServiceRegistryCollection();
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient(new ServerAddress(
                casProperties.getMongoServiceRegistry().getHost(),
                casProperties.getMongoServiceRegistry().getPort()),
                Collections.singletonList(
                        MongoCredential.createCredential(casProperties.getMongoServiceRegistry().getUserId(),
                                getDatabaseName(),
                                casProperties.getMongoServiceRegistry().getUserPassword().toCharArray())),
                mongoClientOptions());
    }

    /**
     * Mongo mongo client options factory bean.
     *
     * @return the mongo client options factory bean
     */
    @RefreshScope
    @Bean
    public MongoClientOptions mongoClientOptions() {
        try {
            final MongoClientOptionsFactoryBean bean = new MongoClientOptionsFactoryBean();
            bean.setWriteConcern(WriteConcern.valueOf(casProperties.getMongoServiceRegistry().getWriteConcern()));
            bean.setHeartbeatConnectTimeout(casProperties.getMongoServiceRegistry().getTimeout());
            bean.setHeartbeatSocketTimeout(casProperties.getMongoServiceRegistry().getTimeout());
            bean.setMaxConnectionLifeTime(casProperties.getMongoServiceRegistry().getConns().getLifetime());
            bean.setSocketKeepAlive(casProperties.getMongoServiceRegistry().isSocketKeepAlive());
            bean.setMaxConnectionIdleTime(casProperties.getMongoServiceRegistry().getIdleTimeout());
            bean.setConnectionsPerHost(casProperties.getMongoServiceRegistry().getConns().getPerHost());
            bean.setSocketTimeout(casProperties.getMongoServiceRegistry().getTimeout());
            bean.setConnectTimeout(casProperties.getMongoServiceRegistry().getTimeout());
            bean.afterPropertiesSet();
            return bean.getObject();
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
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
    public ServiceRegistryDao mongoServiceRegistryDao() throws Exception {
        return new MongoServiceRegistryDao(
                mongoTemplate(),
                casProperties.getMongoServiceRegistry().getServiceRegistryCollection(),
                casProperties.getMongoServiceRegistry().isDropCollection());
    }
}
