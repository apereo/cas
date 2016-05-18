package org.apereo.cas.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import org.apereo.cas.services.convert.BaseConverters;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${cas.service.registry.mongo.db:cas-service-registry}")
    private String databaseName;

    @Value("${mongodb.userId:}")
    private String username;

    @Value("${mongodb.userPassword:}")
    private String password;

    @Value("${mongodb.host:localhost}")
    private String host;

    @Value("${mongodb.port:27017}")
    private int port;

    @Value("${mongodb.writeconcern:NORMAL}")
    private WriteConcern writeConcern;

    @Value("${mongodb.timeout:5000}")
    private int heartBeatConnectionTimeout;

    @Value("${mongodb.timeout:5000}")
    private int heartBeatSocketTimeout;

    @Value("${mongodb.conns.lifetime:60000}")
    private int maxConnectionsLifeTime;

    @Value("${mongodb.socket.keepalive:false}")
    private boolean socketKeepAlive;

    @Value("${mongodb.idle.timeout:30000}")
    private int idleTime;

    @Value("${mongodb.timeout:5000}")
    private int connectTimeout;

    @Value("${mongodb.timeout:5000}")
    private int socketTimeout;

    @Value("${mongodb.conns.per.host:10}")
    private int connectionsPerHost;

    /**
     * Persistence exception translation post processor persistence exception translation post processor.
     *
     * @return the persistence exception translation post processor
     */
    @Bean(name = "persistenceExceptionTranslationPostProcessor")
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Override
    protected String getDatabaseName() {
        return this.databaseName;
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient(new ServerAddress(this.host, this.port),
                Collections.singletonList(MongoCredential.createCredential(this.username,
                        this.databaseName, this.password.toCharArray())),
                mongoClientOptions());
    }

    /**
     * Mongo mongo client options factory bean.
     *
     * @return the mongo client options factory bean
     */
    @RefreshScope
    @Bean(name = "mongoClientOptions")
    public MongoClientOptions mongoClientOptions() {
        try {
            final MongoClientOptionsFactoryBean bean = new MongoClientOptionsFactoryBean();
            bean.setWriteConcern(this.writeConcern);
            bean.setHeartbeatConnectTimeout(this.heartBeatConnectionTimeout);
            bean.setHeartbeatSocketTimeout(this.heartBeatSocketTimeout);
            bean.setMaxConnectionLifeTime(this.maxConnectionsLifeTime);
            bean.setSocketKeepAlive(this.socketKeepAlive);
            bean.setMaxConnectionIdleTime(this.idleTime);
            bean.setConnectionsPerHost(this.connectionsPerHost);
            bean.setSocketTimeout(this.socketTimeout);
            bean.setConnectTimeout(this.connectTimeout);
            bean.afterPropertiesSet();
            return bean.getObject();
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
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


}
