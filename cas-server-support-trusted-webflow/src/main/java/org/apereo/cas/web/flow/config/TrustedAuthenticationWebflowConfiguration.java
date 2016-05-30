package org.apereo.cas.web.flow.config;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.TrustedAuthenticationWebflowConfigurer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link TrustedAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("trustedAuthenticationWebflowConfiguration")
public class TrustedAuthenticationWebflowConfiguration {
    
    @Bean
    @RefreshScope
    public CasWebflowConfigurer trustedWebflowConfigurer() {
        return new TrustedAuthenticationWebflowConfigurer();
    }

}
