package org.apereo.cas.web.flow.config;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.RemoteAddressWebflowConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasRemoteAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casRemoteAuthenticationConfiguration")
public class CasRemoteAuthenticationConfiguration {
    
    @Bean   
    public CasWebflowConfigurer remoteAddressWebflowConfigurer() {
        return new RemoteAddressWebflowConfigurer();
    }
}
