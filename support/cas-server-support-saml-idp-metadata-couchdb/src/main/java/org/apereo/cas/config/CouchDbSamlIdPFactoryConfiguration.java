package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.DefaultCouchDbConnectorFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CouchDbSamlIdPFactoryConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 * @deprecated Since 7
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProvider, module = "couchdb")
@AutoConfiguration
@Deprecated(since = "7.0.0")
public class CouchDbSamlIdPFactoryConfiguration {

    @ConditionalOnMissingBean(name = "samlMetadataCouchDbFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public CouchDbConnectorFactory samlMetadataCouchDbFactory(
        final CasConfigurationProperties casProperties,
        @Qualifier("defaultObjectMapperFactory")
        final ObjectMapperFactory objectMapperFactory) {
        return new DefaultCouchDbConnectorFactory(casProperties.getAuthn().getSamlIdp().getMetadata().getCouchDb(), objectMapperFactory);
    }
}
