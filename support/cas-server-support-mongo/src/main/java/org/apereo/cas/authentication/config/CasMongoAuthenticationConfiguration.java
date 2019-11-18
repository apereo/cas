package org.apereo.cas.authentication.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MongoDbAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder;
import org.pac4j.mongo.profile.service.MongoProfileService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasMongoAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("casMongoAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasMongoAuthenticationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = "mongoPrincipalFactory")
    @Bean
    public PrincipalFactory mongoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler mongoAuthenticationHandler() {
        val mongo = casProperties.getAuthn().getMongo();
        val handler = new MongoDbAuthenticationHandler(mongo.getName(), servicesManager.getObject(), mongoPrincipalFactory());
        handler.setAuthenticator(mongoAuthenticatorProfileService());
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(mongo.getPrincipalTransformation()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "mongoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer mongoAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(mongoAuthenticationHandler(), defaultPrincipalResolver.getObject());
    }

    @ConditionalOnMissingBean(name = "mongoAuthenticatorProfileService")
    @Bean
    public MongoProfileService mongoAuthenticatorProfileService() {
        val mongo = casProperties.getAuthn().getMongo();
        val client = MongoDbConnectionFactory.buildMongoDbClient(mongo);
        LOGGER.info("Connected to MongoDb instance using mongo client [{}]", client.toString());

        val encoder = new SpringSecurityPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(mongo.getPasswordEncoder(), applicationContext));
        val auth = new MongoProfileService(client, mongo.getAttributes());
        auth.setUsersCollection(mongo.getCollection());
        auth.setUsersDatabase(mongo.getDatabaseName());
        auth.setUsernameAttribute(mongo.getUsernameAttribute());
        auth.setPasswordAttribute(mongo.getPasswordAttribute());

        if (StringUtils.isNotBlank(mongo.getPrincipalIdAttribute())) {
            auth.setIdAttribute(mongo.getPrincipalIdAttribute());
        }
        auth.setPasswordEncoder(encoder);
        return auth;
    }
}
