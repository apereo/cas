package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCouchDbAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCouchDbProfileAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.DefaultCouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.DefaultProfileCouchDbRepository;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.couchdb.surrogate.SurrogateAuthorizationCouchDbRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SurrogateCouchDbAuthenticationServiceConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 * @deprecated Since 7
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication, module = "couchdb")
@AutoConfiguration
@Deprecated(since = "7.0.0")
public class SurrogateCouchDbAuthenticationServiceConfiguration {

    @ConditionalOnMissingBean(name = "surrogateCouchDbFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public CouchDbConnectorFactory surrogateCouchDbFactory(
        final CasConfigurationProperties casProperties,
        @Qualifier("defaultObjectMapperFactory")
        final ObjectMapperFactory objectMapperFactory) {
        return new DefaultCouchDbConnectorFactory(casProperties.getAuthn().getSurrogate().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "surrogateCouchDbInstance")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public CouchDbInstance surrogateCouchDbInstance(
        @Qualifier("surrogateCouchDbFactory")
        final CouchDbConnectorFactory surrogateCouchDbFactory) {
        return surrogateCouchDbFactory.createInstance();
    }

    @ConditionalOnMissingBean(name = "surrogateCouchDbConnector")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public CouchDbConnector surrogateCouchDbConnector(
        @Qualifier("surrogateCouchDbFactory")
        final CouchDbConnectorFactory surrogateCouchDbFactory) {
        return surrogateCouchDbFactory.createConnector();
    }

    @ConditionalOnMissingBean(name = "surrogateAuthorizationCouchDbRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SurrogateAuthorizationCouchDbRepository surrogateAuthorizationCouchDbRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("surrogateCouchDbFactory")
        final CouchDbConnectorFactory surrogateCouchDbFactory) {
        val couch = casProperties.getAuthn().getSurrogate().getCouchDb();
        return new SurrogateAuthorizationCouchDbRepository(surrogateCouchDbFactory.getCouchDbConnector(), couch.isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "surrogateAuthorizationProfileCouchDbRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ProfileCouchDbRepository surrogateAuthorizationProfileCouchDbRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("surrogateCouchDbFactory")
        final CouchDbConnectorFactory surrogateCouchDbFactory) {
        val couch = casProperties.getAuthn().getSurrogate().getCouchDb();
        return new DefaultProfileCouchDbRepository(surrogateCouchDbFactory.getCouchDbConnector(), couch.isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "couchDbSurrogateAuthenticationService")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SurrogateAuthenticationService surrogateAuthenticationService(
        final CasConfigurationProperties casProperties,
        @Qualifier("surrogateAuthorizationProfileCouchDbRepository")
        final ProfileCouchDbRepository surrogateAuthorizationProfileCouchDbRepository,
        @Qualifier("surrogateAuthorizationCouchDbRepository")
        final SurrogateAuthorizationCouchDbRepository surrogateAuthorizationCouchDbRepository,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val couchDb = casProperties.getAuthn().getSurrogate().getCouchDb();
        if (couchDb.isProfileBased()) {
            return new SurrogateCouchDbProfileAuthenticationService(surrogateAuthorizationProfileCouchDbRepository,
                couchDb.getSurrogatePrincipalsAttribute(), servicesManager);
        }
        return new SurrogateCouchDbAuthenticationService(surrogateAuthorizationCouchDbRepository, servicesManager);
    }
}
