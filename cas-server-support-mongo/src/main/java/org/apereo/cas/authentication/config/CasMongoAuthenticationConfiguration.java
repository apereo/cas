package org.apereo.cas.authentication.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MongoAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired(required = false)
    @Qualifier("mongoPac4jPasswordEncoder")
    private org.pac4j.core.credentials.password.PasswordEncoder mongoPasswordEncoder;

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

    @Bean
    public PrincipalFactory mongoPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler mongoAuthenticationHandler() {
        final MongoAuthenticationHandler mongo = new MongoAuthenticationHandler();

        mongo.setAttributes(casProperties.getAuthn().getMongo().getAttributes());
        mongo.setCollectionName(casProperties.getAuthn().getMongo().getCollectionName());
        mongo.setMongoHostUri(casProperties.getAuthn().getMongo().getMongoHostUri());
        mongo.setPasswordAttribute(casProperties.getAuthn().getMongo().getPasswordAttribute());
        mongo.setUsernameAttribute(casProperties.getAuthn().getMongo().getUsernameAttribute());

        mongo.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(
                casProperties.getAuthn().getMongo().getPrincipalTransformation()));

        mongo.setPasswordEncoder(Beans.newPasswordEncoder(casProperties.getAuthn().getMongo().getPasswordEncoder()));
        if (mongoPasswordEncoder != null) {
            mongo.setMongoPasswordEncoder(mongoPasswordEncoder);
        }


        mongo.setPrincipalFactory(mongoPrincipalFactory());
        mongo.setServicesManager(servicesManager);

        return mongo;
    }


    @PostConstruct
    public void initializeAuthenticationHandler() {
        this.authenticationHandlersResolvers.put(mongoAuthenticationHandler(),
                personDirectoryPrincipalResolver);
    }
}
