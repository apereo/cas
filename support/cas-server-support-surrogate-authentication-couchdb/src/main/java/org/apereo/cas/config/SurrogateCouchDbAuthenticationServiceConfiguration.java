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
import org.springframework.beans.factory.ObjectProvider;
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
@Configuration("SamlIdPCouchDbMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateCouchDbAuthenticationServiceConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectProvider<ObjectMapperFactory> objectMapperFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("surrogateCouchDbFactory")
    private ObjectProvider<CouchDbConnectorFactory> surrogateCouchDbFactory;

    @ConditionalOnMissingBean(name = "surrogateCouchDbFactory")
    @RefreshScope
    @Bean
    public CouchDbConnectorFactory surrogateCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getSurrogate().getCouchDb(), objectMapperFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "surrogateCouchDbInstance")
    @RefreshScope
    @Bean
    public CouchDbInstance surrogateCouchDbInstance() {
        return surrogateCouchDbFactory.getObject().createInstance();
    }

    @ConditionalOnMissingBean(name = "surrogateCouchDbConnector")
    @RefreshScope
    @Bean
    public CouchDbConnector surrogateCouchDbConnector() {
        return surrogateCouchDbFactory.getObject().createConnector();
    }

    @ConditionalOnMissingBean(name = "surrogateAuthorizationCouchDbRepository")
    @Bean
    @RefreshScope
    public SurrogateAuthorizationCouchDbRepository surrogateAuthorizationCouchDbRepository() {
        val couch = casProperties.getAuthn().getSurrogate().getCouchDb();
        return new SurrogateAuthorizationCouchDbRepository(surrogateCouchDbFactory.getObject().getCouchDbConnector(), couch.isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "surrogateAuthorizationProfileCouchDbRepository")
    @Bean
    @RefreshScope
    public ProfileCouchDbRepository surrogateAuthorizationProfileCouchDbRepository() {
        val couch = casProperties.getAuthn().getSurrogate().getCouchDb();
        return new ProfileCouchDbRepository(surrogateCouchDbFactory.getObject().getCouchDbConnector(), couch.isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "couchDbSurrogateAuthenticationService")
    @Bean
    @RefreshScope
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        val couchDb = casProperties.getAuthn().getSurrogate().getCouchDb();
        if (couchDb.isProfileBased()) {
            return new SurrogateCouchDbProfileAuthenticationService(surrogateAuthorizationProfileCouchDbRepository(),
                couchDb.getSurrogatePrincipalsAttribute(), servicesManager.getObject());
        }
        return new SurrogateCouchDbAuthenticationService(surrogateAuthorizationCouchDbRepository(), servicesManager.getObject());

    }
}
