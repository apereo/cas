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
import org.ektorp.impl.ObjectMapperFactory;
import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder;
import org.pac4j.couch.profile.service.CouchProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CouchDbAuthenticationConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "couchDbAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CouchDbAuthenticationConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "authenticationCouchDbFactory")
    @Autowired
    public CouchDbConnectorFactory authenticationCouchDbFactory(final CasConfigurationProperties casProperties,
                                                                @Qualifier("defaultObjectMapperFactory")
                                                                final ObjectMapperFactory objectMapperFactory) {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "authenticationCouchDbRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public ProfileCouchDbRepository authenticationCouchDbRepository(
        @Qualifier("authenticationCouchDbFactory")
        final CouchDbConnectorFactory authenticationCouchDbFactory, final CasConfigurationProperties casProperties) {
        return new ProfileCouchDbRepository(authenticationCouchDbFactory.getCouchDbConnector(), casProperties.getAuthn().getCouchDb().isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "couchDbAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer couchDbAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("couchDbAuthenticationHandler")
        final AuthenticationHandler authenticationHandler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(authenticationHandler, defaultPrincipalResolver);
    }

    @ConditionalOnMissingBean(name = "couchDbPrincipalFactory")
    @Bean
    public PrincipalFactory couchDbPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "couchDbAuthenticationHandler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationHandler couchDbAuthenticationHandler(
        @Qualifier("couchDbAuthenticatorProfileService")
        final CouchProfileService couchProfileService,
        @Qualifier("couchDbPrincipalFactory")
        final PrincipalFactory principalFactory, final CasConfigurationProperties casProperties,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val couchDb = casProperties.getAuthn().getCouchDb();
        val handler = new CouchDbAuthenticationHandler(couchDb.getName(), servicesManager, principalFactory, couchDb.getOrder());
        handler.setAuthenticator(couchProfileService);
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(couchDb.getPrincipalTransformation()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "couchDbAuthenticatorProfileService")
    @Bean
    @Autowired
    public CouchProfileService couchDbAuthenticatorProfileService(
        @Qualifier("authenticationCouchDbFactory")
        final CouchDbConnectorFactory authenticationCouchDbFactory, final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext) {
        val couchDb = casProperties.getAuthn().getCouchDb();
        LOGGER.info("Connected to CouchDb instance @ [{}] using database [{}]", couchDb.getUrl(), couchDb.getDbName());
        val encoder = new SpringSecurityPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(couchDb.getPasswordEncoder(), applicationContext));
        val auth = new CouchProfileService(authenticationCouchDbFactory.getCouchDbConnector(), couchDb.getAttributes());
        auth.setUsernameAttribute(couchDb.getUsernameAttribute());
        auth.setPasswordAttribute(couchDb.getPasswordAttribute());
        auth.setPasswordEncoder(encoder);
        return auth;
    }
}
