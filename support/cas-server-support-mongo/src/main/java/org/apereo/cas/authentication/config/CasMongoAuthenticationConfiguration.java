package org.apereo.cas.authentication.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MongoAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
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

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This is {@link CasMongoAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casMongoAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasMongoAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @ConditionalOnMissingBean(name = "mongoPrincipalFactory")
    @Bean
    public PrincipalFactory mongoPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler mongoAuthenticationHandler() {
        final MongoAuthenticationHandler handler = new MongoAuthenticationHandler();
        final MongoAuthenticationProperties mongo = casProperties.getAuthn().getMongo();

        handler.setAttributes(mongo.getAttributes());
        handler.setCollectionName(mongo.getCollectionName());
        handler.setMongoHostUri(mongo.getMongoHostUri());
        handler.setPasswordAttribute(mongo.getPasswordAttribute());
        handler.setUsernameAttribute(mongo.getUsernameAttribute());
        handler.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(mongo.getPrincipalTransformation()));
        handler.setMongoPasswordEncoder(new SpringSecurityPasswordEncoder(Beans.newPasswordEncoder(mongo.getPasswordEncoder())));
        handler.setPrincipalFactory(mongoPrincipalFactory());
        handler.setServicesManager(servicesManager);
        handler.setName(mongo.getName());

        return handler;
    }


    @PostConstruct
    public void initializeAuthenticationHandler() {
        this.authenticationHandlersResolvers.put(mongoAuthenticationHandler(), personDirectoryPrincipalResolver);
    }
}
