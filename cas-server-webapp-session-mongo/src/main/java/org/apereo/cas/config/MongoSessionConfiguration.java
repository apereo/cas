package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@Configuration("mongoSessionConfiguration")
@EnableMongoHttpSession
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoSessionConfiguration {
    
    /**
     * Jdk mongo session converter jdk mongo session converter.
     *
     * @return the jdk mongo session converter
     */
    @Bean
    public JdkMongoSessionConverter jdkMongoSessionConverter() {
        return new JdkMongoSessionConverter();
    }
}
