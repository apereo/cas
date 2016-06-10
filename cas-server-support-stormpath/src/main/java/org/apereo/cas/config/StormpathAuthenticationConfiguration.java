package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.StormpathAuthenticationHandler;
import org.apereo.cas.authentication.handler.PasswordEncoder;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.pac4j.http.credentials.password.NopPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link StormpathAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("stormpathAuthenticationConfiguration")
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
        
        return handler;
    }
}
