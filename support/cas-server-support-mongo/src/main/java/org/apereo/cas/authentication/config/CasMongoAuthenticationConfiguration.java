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

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

/**
 * This is {@link CasMongoAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("casMongoAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
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

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @ConditionalOnMissingBean(name = "mongoPrincipalFactory")
    @Bean
    public PrincipalFactory mongoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "mongoAuthenticationHandler")
    public AuthenticationHandler mongoAuthenticationHandler() {
        val mongo = casProperties.getAuthn().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext.getObject());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        val handler = new MongoDbAuthenticationHandler(mongo.getName(),
            servicesManager.getObject(), mongoPrincipalFactory(), mongo, mongoTemplate);
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(mongo.getPasswordEncoder(), applicationContext));
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(mongo.getPrincipalTransformation()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "mongoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlanConfigurer mongoAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(mongoAuthenticationHandler(), defaultPrincipalResolver.getObject());
    }
}
