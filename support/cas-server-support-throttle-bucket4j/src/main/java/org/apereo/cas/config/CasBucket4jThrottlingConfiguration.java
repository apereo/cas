package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasBucket4jThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("casBucket4jThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasBucket4jThrottlingConfiguration {

}
