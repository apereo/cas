package org.apereo.cas.config;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

/**
 * This is {@link MongoSessionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Configuration("mongoSessionConfiguration")
@EnableMongoHttpSession
public class MongoSessionConfiguration {
    
    /**
     * Jdk mongo session converter jdk mongo session converter.
     *
     * @return the jdk mongo session converter
     */
    @Bean(name="jdkMongoSessionConverter")
    public JdkMongoSessionConverter jdkMongoSessionConverter() {
        return new JdkMongoSessionConverter();
    }
}
