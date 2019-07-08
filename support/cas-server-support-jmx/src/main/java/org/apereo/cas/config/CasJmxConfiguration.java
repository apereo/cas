package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;

/**
 * This is {@link CasJmxConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("casJmxConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableMBeanExport
public class CasJmxConfiguration {
}
