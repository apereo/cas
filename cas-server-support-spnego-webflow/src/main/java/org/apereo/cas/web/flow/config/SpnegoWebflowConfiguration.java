package org.apereo.cas.web.flow.config;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.SpengoWebflowConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SpnegoWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("spnegoWebflowConfiguration")
public class SpnegoWebflowConfiguration {
    
    @Bean
    public CasWebflowConfigurer spnegoWebflowConfigurer() {
        return new SpengoWebflowConfigurer();
    }
}
