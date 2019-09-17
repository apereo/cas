package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.jdbc.config.annotation.web.http.JdbcHttpSessionConfiguration;

/**
 * This is {@link JdbcSessionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "jdbcSessionConfiguration", proxyBeanMethods = false)
@EnableJdbcHttpSession
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Import({JdbcHttpSessionConfiguration.class, DataSourceAutoConfiguration.class})
public class JdbcSessionConfiguration {
}
