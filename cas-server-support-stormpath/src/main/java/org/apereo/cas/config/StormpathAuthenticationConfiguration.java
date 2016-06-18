package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.StormpathAuthenticationHandler;
import org.apereo.cas.authentication.handler.PasswordEncoder;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.http.credentials.password.NopPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link StormpathAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("stormpathAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class StormpathAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("stormpathPasswordEncoder")
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    @Qualifier("stormpathPac4jPasswordEncoder")
    private org.pac4j.http.credentials.password.PasswordEncoder stormpathPasswordEncoder
            = new NopPasswordEncoder();

    @Autowired(required = false)
    @Qualifier("stormpathPrincipalNameTransformer")
    private PrincipalNameTransformer principalNameTransformer;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    public PrincipalFactory stormpathPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public AuthenticationHandler stormpathAuthenticationHandler() {
        final StormpathAuthenticationHandler handler =
                new StormpathAuthenticationHandler(
                        casProperties.getAuthn().getStormpath().getApiKey(),
                        casProperties.getAuthn().getStormpath().getApplicationId(),
                        casProperties.getAuthn().getStormpath().getSecretkey());

        if (this.passwordEncoder != null) {
            handler.setPasswordEncoder(this.passwordEncoder);
        }
        if (this.principalNameTransformer != null) {
            handler.setPrincipalNameTransformer(this.principalNameTransformer);
        }

        handler.setStormpathPasswordEncoder(this.stormpathPasswordEncoder);

        handler.setPrincipalFactory(stormpathPrincipalFactory());
        handler.setServicesManager(servicesManager);
        return handler;
    }
}
