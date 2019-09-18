package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * This is {@link CasMetricsRepositoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casMetricsRepositoryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Getter
public class CasMetricsRepositoryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
}
