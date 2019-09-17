package org.apereo.cas.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link ConditionalDataSourceConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ConditionalOnProperty(name = "spring.datasource.url")
@Configuration(value = "conditionalDataSourceConfiguration", proxyBeanMethods = false)
@Import(DataSourceAutoConfiguration.class)
public class ConditionalDataSourceConfiguration {
}
