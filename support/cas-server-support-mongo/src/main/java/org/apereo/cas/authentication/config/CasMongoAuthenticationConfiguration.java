package org.apereo.cas.authentication.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MongoAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.MongoAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
public class CasMongoAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;
    
    @ConditionalOnMissingBean(name = "mongoPrincipalFactory")
    @Bean
    public PrincipalFactory mongoPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler mongoAuthenticationHandler() {
        final MongoAuthenticationProperties mongo = casProperties.getAuthn().getMongo();
        final SpringSecurityPasswordEncoder mongoPasswordEncoder = new SpringSecurityPasswordEncoder(Beans.newPasswordEncoder(mongo.getPasswordEncoder()));
        final MongoAuthenticationHandler handler = new MongoAuthenticationHandler(mongo.getName(), servicesManager, mongoPrincipalFactory(),
                mongo.getCollectionName(), mongo.getMongoHostUri(), mongo.getAttributes(), mongo.getUsernameAttribute(), mongo.getPasswordAttribute(),
                mongoPasswordEncoder);
        handler.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(mongo.getPrincipalTransformation()));

        return handler;
    }

    @ConditionalOnMissingBean(name = "mongoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer mongoAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(mongoAuthenticationHandler(), personDirectoryPrincipalResolver);
    }
}
