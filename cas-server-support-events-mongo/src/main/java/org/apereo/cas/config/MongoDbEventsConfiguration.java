package org.apereo.cas.config;

import com.mongodb.MongoClientURI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

/**
 * This is {@link MongoDbEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("mongoDbEventsConfiguration")
public class MongoDbEventsConfiguration {

    /**
     * The Client uri.
     */
    @Value("${mongodb.events.clienturi:}")
    private String clientUri;

    /**
     * Client uri mongo client uri.
     *
     * @return the mongo client uri
     */
    @RefreshScope
    @Bean(name = "clientUri")
    public MongoClientURI clientUri() {
        if (StringUtils.isBlank(this.clientUri)) {
            throw new RuntimeException("MongoDb Client URI must be defined for CAS events");
        }
        return new MongoClientURI(this.clientUri);
    }


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
     * Mongo events template mongo template.
     *
     * @return the mongo template
     */
    @RefreshScope
    @Bean(name = "mongoEventsTemplate")
    public MongoTemplate mongoEventsTemplate() {
        return new MongoTemplate(mongoAuthNEventsDbFactory());
    }

    /**
     * Mongo auth n events db factory simple mongo db factory.
     *
     * @return the simple mongo db factory
     */
    @RefreshScope
    @Bean(name = "mongoAuthNEventsDbFactory")
    public SimpleMongoDbFactory mongoAuthNEventsDbFactory() {

        try {
            return new SimpleMongoDbFactory(clientUri());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
