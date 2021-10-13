package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;

import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CouchDbSamlIdPFactoryConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "couchDbSamlIdPFactoryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchDbSamlIdPFactoryConfiguration {

    @ConditionalOnMissingBean(name = "samlMetadataCouchDbFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public CouchDbConnectorFactory samlMetadataCouchDbFactory(final CasConfigurationProperties casProperties,
                                                              @Qualifier("defaultObjectMapperFactory")
                                                              final ObjectMapperFactory objectMapperFactory) {
        return new CouchDbConnectorFactory(casProperties.getAuthn()
            .getSamlIdp()
            .getMetadata()
            .getCouchDb(), objectMapperFactory);
    }
}
