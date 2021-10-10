package org.apereo.cas.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link ConditionalDataSourceAutoConfiguration}.
 * This will import {@link DataSourceAutoConfiguration}
 * but only conditionally, if a datasource url is found in properties.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Configuration(value = "ConditionalDataSourceAutoConfiguration", proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.datasource.url")
@ImportAutoConfiguration(DataSourceAutoConfiguration.class)
public class ConditionalDataSourceAutoConfiguration {
}
