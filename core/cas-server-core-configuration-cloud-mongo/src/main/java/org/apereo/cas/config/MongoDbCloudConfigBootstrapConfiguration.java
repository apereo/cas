package org.apereo.cas.config;

import com.google.common.base.Throwables;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apereo.cas.MongoDbPropertySource;
import org.apereo.cas.MongoDbPropertySourceLocator;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoClientOptionsFactoryBean;

import java.util.Collections;

/**
 * This is {@link MongoDbCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("mongoDbCloudConfigBootstrapConfiguration")
@ConditionalOnProperty(name = "cas.spring.cloud.mongo.uri")
public class MongoDbCloudConfigBootstrapConfiguration extends AbstractMongoConfiguration {
    private static final int TIMEOUT = 5000;
    private static final int DEFAULT_PORT = 27017;
    
    @Autowired
    private ConfigurableEnvironment environment;

    @Bean
    public MongoDbPropertySourceLocator consulPropertySourceLocator() {
        try {
            if (!mongoTemplate().collectionExists(MongoDbPropertySource.class.getSimpleName())) {
                mongoTemplate().createCollection(MongoDbPropertySource.class.getSimpleName());
            }
            return new MongoDbPropertySourceLocator(mongoTemplate());
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected String getDatabaseName() {
        return mongoClientUri().getDatabase();
    }

    @Override
    public Mongo mongo() throws Exception {
        final MongoCredential credential = MongoCredential.createCredential(
                mongoClientUri().getUsername(),
                getDatabaseName(),
                mongoClientUri().getPassword());

        final String hostUri = mongoClientUri().getHosts().get(0);
        final String[] host = hostUri.split(":");
        return new MongoClient(new ServerAddress(
                host[0], host.length > 1 ? Integer.parseInt(host[1]) : DEFAULT_PORT),
                Collections.singletonList(credential),
                mongoClientOptions());
    }

    @Bean
    public MongoClientOptions mongoClientOptions() {
        try {
            final MongoClientOptionsFactoryBean bean = new MongoClientOptionsFactoryBean();
            bean.setSocketTimeout(TIMEOUT);
            bean.setConnectTimeout(TIMEOUT);
            bean.afterPropertiesSet();
            return bean.getObject();
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public MongoClientURI mongoClientUri() {
        return new MongoClientURI(environment.getProperty("cas.spring.cloud.mongo.uri"));
    }
}
