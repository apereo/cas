package org.apereo.cas.authentication.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MongoAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.MongoAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder;
import org.pac4j.mongo.profile.service.MongoProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CasMongoAuthenticationConfiguration.class);
    
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
        final MongoAuthenticationHandler handler = new MongoAuthenticationHandler(mongo.getName(), servicesManager, mongoPrincipalFactory());
        handler.setAuthenticator(mongoAuthenticatorProfileService());
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(mongo.getPrincipalTransformation()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "mongoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer mongoAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(mongoAuthenticationHandler(), personDirectoryPrincipalResolver);
    }

    @ConditionalOnMissingBean(name = "mongoAuthenticatorProfileService")
    @Bean
    public MongoProfileService mongoAuthenticatorProfileService() {
        final MongoAuthenticationProperties mongo = casProperties.getAuthn().getMongo();
        
        final MongoClientURI uri = new MongoClientURI(mongo.getMongoHostUri());
        final MongoClient client = new MongoClient(uri);
        LOGGER.info("Connected to MongoDb instance @ [{}] using database [{}]", uri.getHosts(), uri.getDatabase());

        final SpringSecurityPasswordEncoder encoder = new SpringSecurityPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(mongo.getPasswordEncoder()));
        final MongoProfileService auth = new MongoProfileService(client, mongo.getAttributes());
        auth.setUsersCollection(mongo.getCollectionName());
        auth.setUsersDatabase(uri.getDatabase());
        auth.setUsernameAttribute(mongo.getUsernameAttribute());
        auth.setPasswordAttribute(mongo.getPasswordAttribute());
        auth.setPasswordEncoder(encoder);
        return auth;
    }
}
