package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasConsentApiConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casConsentApiConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConsentApiConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasConsentApiConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

}
