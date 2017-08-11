package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasInterruptConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casInterruptConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasInterruptConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;
    
}
