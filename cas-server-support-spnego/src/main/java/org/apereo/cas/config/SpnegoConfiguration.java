package org.apereo.cas.config;

import jcifs.spnego.Authentication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SpnegoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("spnegoConfiguration")
public class SpnegoConfiguration {

    /**
     * Spnego authentication.
     *
     * @return the authentication
     */
    @RefreshScope
    @Bean(name="spnegoAuthentication")
    public Authentication spnegoAuthentication() {
        return new Authentication();
    }
}
