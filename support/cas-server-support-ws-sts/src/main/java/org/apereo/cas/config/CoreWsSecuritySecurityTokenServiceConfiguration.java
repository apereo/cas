package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CoreWsSecuritySecurityTokenServiceConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("coreWsSecuritySecurityTokenServiceConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CoreWsSecuritySecurityTokenServiceConfiguration {
    
    @Autowired
    private CasConfigurationProperties casProperties;

}
