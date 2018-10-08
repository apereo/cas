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

/**
 * This is {@link CouchDbSamlIdPFactoryConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("couchDbSamlIdPFactoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchDbSamlIdPFactoryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectMapperFactory objectMapperFactory;

    @ConditionalOnMissingBean(name = "samlMetadataCouchDbFactory")
    @RefreshScope
    @Bean
    public CouchDbConnectorFactory samlMetadataCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getSamlIdp().getMetadata().getCouchDb(), objectMapperFactory);
    }
}
