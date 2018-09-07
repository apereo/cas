package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CouchDbAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.impl.ObjectMapperFactory;
import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder;
import org.pac4j.couch.profile.service.CouchProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CouchDbAuthenticationConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("couchDbAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CouchDbAuthenticationConfiguration {

    @Autowired
    private ObjectMapperFactory objectMapperFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("authenticationCouchDbFactory")
    private CouchDbConnectorFactory authenticationCouchDbFactory;

    @ConditionalOnMissingBean(name = "couchDbPrincipalFactory")
    @Bean
    public PrincipalFactory couchDbPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler couchDbAuthenticationHandler() {
        val couchDb = casProperties.getAuthn().getCouchDb();
        val handler = new CouchDbAuthenticationHandler(couchDb.getName(), servicesManager, couchDbPrincipalFactory(), couchDb.getOrder());
        handler.setAuthenticator(couchDbAuthenticatorProfileService());
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(couchDb.getPrincipalTransformation()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "couchDbAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer couchDbAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(couchDbAuthenticationHandler(), personDirectoryPrincipalResolver);
    }

    @Bean
    @RefreshScope
    public CouchDbConnectorFactory authenticationCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getCouchDb(), objectMapperFactory);
    }

    @Bean
    @RefreshScope
    public ProfileCouchDbRepository authenticationCouchDbRepository() {
        val repository = new ProfileCouchDbRepository(authenticationCouchDbConnector(),
            casProperties.getAuthn().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "couchDbAuthenticatorProfileService")
    @Bean
    public CouchProfileService couchDbAuthenticatorProfileService() {
        val couchDb = casProperties.getAuthn().getCouchDb();

        LOGGER.info("Connected to CouchDb instance @ [{}] using database [{}]", couchDb.getUrl(), couchDb.getDbName());

        val encoder = new SpringSecurityPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(couchDb.getPasswordEncoder()));
        val auth = new CouchProfileService(authenticationCouchDbConnector(), couchDb.getAttributes());
        auth.setUsernameAttribute(couchDb.getUsernameAttribute());
        auth.setPasswordAttribute(couchDb.getPasswordAttribute());
        auth.setPasswordEncoder(encoder);
        return auth;
    }

    @RefreshScope
    @Bean
    public CouchDbInstance authenticationCouchDbInstance() {
        return authenticationCouchDbFactory.createInstance();
    }

    @RefreshScope
    @Bean
    public CouchDbConnector authenticationCouchDbConnector() {
        return authenticationCouchDbFactory.createConnector();
    }
}
