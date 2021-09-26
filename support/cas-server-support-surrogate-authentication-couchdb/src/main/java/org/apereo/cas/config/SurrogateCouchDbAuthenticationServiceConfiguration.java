package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCouchDbAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCouchDbProfileAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.couchdb.surrogate.SurrogateAuthorizationCouchDbRepository;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateCouchDbAuthenticationServiceConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "SamlIdPCouchDbMetadataConfiguration", proxyBeanMethods = false)
public class SurrogateCouchDbAuthenticationServiceConfiguration {

    @ConditionalOnMissingBean(name = "surrogateCouchDbFactory")
    @RefreshScope
    @Bean
    @Autowired
    public CouchDbConnectorFactory surrogateCouchDbFactory(final CasConfigurationProperties casProperties,
                                                           @Qualifier("objectMapperFactory")
                                                           final ObjectMapperFactory objectMapperFactory) {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getSurrogate().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "surrogateCouchDbInstance")
    @RefreshScope
    @Bean
    public CouchDbInstance surrogateCouchDbInstance(
        @Qualifier("surrogateCouchDbFactory")
        final CouchDbConnectorFactory surrogateCouchDbFactory) {
        return surrogateCouchDbFactory.createInstance();
    }

    @ConditionalOnMissingBean(name = "surrogateCouchDbConnector")
    @RefreshScope
    @Bean
    public CouchDbConnector surrogateCouchDbConnector(
        @Qualifier("surrogateCouchDbFactory")
        final CouchDbConnectorFactory surrogateCouchDbFactory) {
        return surrogateCouchDbFactory.createConnector();
    }

    @ConditionalOnMissingBean(name = "surrogateAuthorizationCouchDbRepository")
    @Bean
    @RefreshScope
    @Autowired
    public SurrogateAuthorizationCouchDbRepository surrogateAuthorizationCouchDbRepository(final CasConfigurationProperties casProperties,
                                                                                           @Qualifier("surrogateCouchDbFactory")
                                                                                           final CouchDbConnectorFactory surrogateCouchDbFactory) {
        val couch = casProperties.getAuthn().getSurrogate().getCouchDb();
        return new SurrogateAuthorizationCouchDbRepository(surrogateCouchDbFactory.getCouchDbConnector(), couch.isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "surrogateAuthorizationProfileCouchDbRepository")
    @Bean
    @RefreshScope
    @Autowired
    public ProfileCouchDbRepository surrogateAuthorizationProfileCouchDbRepository(final CasConfigurationProperties casProperties,
                                                                                   @Qualifier("surrogateCouchDbFactory")
                                                                                   final CouchDbConnectorFactory surrogateCouchDbFactory) {
        val couch = casProperties.getAuthn().getSurrogate().getCouchDb();
        return new ProfileCouchDbRepository(surrogateCouchDbFactory.getCouchDbConnector(), couch.isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "couchDbSurrogateAuthenticationService")
    @Bean
    @RefreshScope
    @Autowired
    public SurrogateAuthenticationService surrogateAuthenticationService(final CasConfigurationProperties casProperties,
                                                                         @Qualifier("surrogateAuthorizationProfileCouchDbRepository")
                                                                         final ProfileCouchDbRepository surrogateAuthorizationProfileCouchDbRepository,
                                                                         @Qualifier("surrogateAuthorizationCouchDbRepository")
                                                                         final SurrogateAuthorizationCouchDbRepository surrogateAuthorizationCouchDbRepository,
                                                                         @Qualifier("servicesManager")
                                                                         final ServicesManager servicesManager) {
        val couchDb = casProperties.getAuthn().getSurrogate().getCouchDb();
        if (couchDb.isProfileBased()) {
            return new SurrogateCouchDbProfileAuthenticationService(surrogateAuthorizationProfileCouchDbRepository, couchDb.getSurrogatePrincipalsAttribute(), servicesManager);
        }
        return new SurrogateCouchDbAuthenticationService(surrogateAuthorizationCouchDbRepository, servicesManager);
    }
}
