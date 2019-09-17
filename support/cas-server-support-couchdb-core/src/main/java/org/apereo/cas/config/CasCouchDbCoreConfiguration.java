package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CasObjectMapperFactory;

import lombok.val;
import org.ektorp.ViewQuery;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCouchDbCoreConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "casCouchDbCoreConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCouchDbCoreConfiguration {

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "defaultObjectMapperFactory")
    public ObjectMapperFactory defaultObjectMapperFactory() {
        val objectMapperFactory = new CasObjectMapperFactory();
        ViewQuery.setDefaultObjectMapperFactory(objectMapperFactory);
        return objectMapperFactory;
    }
}
