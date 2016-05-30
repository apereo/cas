package org.apereo.cas.web.flow.config;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.TokenWebflowConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link TokenAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("tokenAuthenticationWebflowConfiguration")
public class TokenAuthenticationWebflowConfiguration {
    
    @Bean
    public CasWebflowConfigurer tokenWebflowConfigurer() {
        return new TokenWebflowConfigurer();
    }
    
}
