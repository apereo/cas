package org.apereo.cas.authentication.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MongoAuthenticationHandler;
import org.apereo.cas.authentication.handler.PasswordEncoder;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasMongoAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casMongoAuthenticationConfiguration")
public class CasMongoAuthenticationConfiguration {

    @Autowired(required = false)
    @Qualifier("mongoPasswordEncoder")
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    @Qualifier("mongoPrincipalNameTransformer")
    private PrincipalNameTransformer principalNameTransformer;

    @Autowired(required = false)
    @Qualifier("mongoPac4jPasswordEncoder")
    private org.pac4j.http.credentials.password.PasswordEncoder mongoPasswordEncoder;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public AuthenticationHandler mongoAuthenticationHandler() {
        final MongoAuthenticationHandler mongo = new MongoAuthenticationHandler();

        mongo.setAttributes(casProperties.getAuthn().getMongo().getAttributes());
        mongo.setCollectionName(casProperties.getAuthn().getMongo().getCollectionName());
        mongo.setMongoHostUri(casProperties.getAuthn().getMongo().getMongoHostUri());
        mongo.setPasswordAttribute(casProperties.getAuthn().getMongo().getPasswordAttribute());
        mongo.setUsernameAttribute(casProperties.getAuthn().getMongo().getUsernameAttribute());

        if (principalNameTransformer != null) {
            mongo.setPrincipalNameTransformer(principalNameTransformer);
        }

        if (passwordEncoder != null) {
            mongo.setPasswordEncoder(passwordEncoder);
        }

        if (mongoPasswordEncoder != null) {
            mongo.setMongoPasswordEncoder(mongoPasswordEncoder);
        }
        
        return mongo;
    }
}
