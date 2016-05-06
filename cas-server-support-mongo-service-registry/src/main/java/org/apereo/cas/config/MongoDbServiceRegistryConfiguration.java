package org.apereo.cas.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoClientOptionsFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * This is {@link MongoDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("mongoDbServiceRegistryConfiguration")
@RefreshScope
public class MongoDbServiceRegistryConfiguration {

    /**
     * The Mapping converter.
     */
    @Autowired
    @Qualifier("mappingConverter")
    private MongoConverter mappingConverter;

    /**
     * The Database name.
     */
    @Value("${cas.service.registry.mongo.db:cas-service-registry}")
    private String databaseName;

    /**
     * The Username.
     */
    @Value("${mongodb.userId:}")
    private String username;

    /**
     * The Password.
     */
    @Value("${mongodb.userPassword:}")
    private String password;

    /**
     * The Host.
     */
    @Value("${mongodb.host:localhost}")
    private String host;

    /**
     * The Port.
     */
    @Value("${mongodb.port:27017}")
    private int port;

    /**
     * The Write concern.
     */
    @Value("${mongodb.writeconcern:NORMAL}")
    private WriteConcern writeConcern;

    /**
     * The Heart beat connection timeout.
     */
    @Value("${mongodb.timeout:5000}")
    private int heartBeatConnectionTimeout;

    /**
     * The Heart beat socket timeout.
     */
    @Value("${mongodb.timeout:5000}")
    private int heartBeatSocketTimeout;

    /**
     * The Max connections life time.
     */
    @Value("${mongodb.conns.lifetime:60000}")
    private int maxConnectionsLifeTime;

    /**
     * The Socket keep alive.
     */
    @Value("${mongodb.socket.keepalive:false}")
    private boolean socketKeepAlive;

    /**
     * The Idle time.
     */
    @Value("${mongodb.idle.timeout:30000}")
    private int idleTime;

    /**
     * The Connect timeout.
     */
    @Value("${mongodb.timeout:5000}")
    private int connectTimeout;

    /**
     * The Socket timeout.
     */
    @Value("${mongodb.timeout:5000}")
    private int socketTimeout;

    /**
     * The Connections per host.
     */
    @Value("${mongodb.conns.per.host:10}")
    private int connectionsPerHost;

    /**
     * Persistence exception translation post processor persistence exception translation post processor.
     *
     * @return the persistence exception translation post processor
     */
    @RefreshScope
    @Bean(name = "persistenceExceptionTranslationPostProcessor")
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    /**
     * Mapping context mongo mapping context.
     *
     * @return the mongo mapping context
     */
    @RefreshScope
    @Bean(name = "mappingContext")
    public MongoMappingContext mappingContext() {
        return new MongoMappingContext();
    }

    /**
     * Mongo template mongo template.
     *
     * @param mongo the mongo
     * @return the mongo template
     */
    @RefreshScope
    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate(final Mongo mongo) {
        return new MongoTemplate(new SimpleMongoDbFactory(mongo,
                this.databaseName,
                new UserCredentials(this.username, this.password)), this.mappingConverter);
    }

    /**
     * Mongo mongo client factory bean.
     *
     * @param mongoClientOptions the mongo client options
     * @return the mongo client factory bean
     */
    @RefreshScope
    @Bean(name = "mongo")
    public MongoClientFactoryBean mongo(final MongoClientOptions mongoClientOptions) {
        final MongoClientFactoryBean bean = new MongoClientFactoryBean();
        bean.setHost(this.host);
        bean.setPort(this.port);
        bean.setMongoClientOptions(mongoClientOptions);
        return bean;
    }

    /**
     * Mongo mongo client options factory bean.
     *
     * @return the mongo client options factory bean
     */
    @RefreshScope
    @Bean(name = "mongoClientOptions")
    public MongoClientOptionsFactoryBean mongo() {
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
        return bean;
    }
}
