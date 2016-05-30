package org.apereo.cas.web.flow.config;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.X509WebflowConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link X509AuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("x509AuthenticationWebflowConfiguration")
public class X509AuthenticationWebflowConfiguration {

    @Bean
    public CasWebflowConfigurer x509WebflowConfigurer() {
        return new X509WebflowConfigurer();
    }
}
