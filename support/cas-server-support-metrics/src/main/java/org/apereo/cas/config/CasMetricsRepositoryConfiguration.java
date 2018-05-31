package org.apereo.cas.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * This is {@link CasMetricsRepositoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casMetricsRepositoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Getter
public class CasMetricsRepositoryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
}
