package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.session.JdbcSessionDataSourceInitializer;
import org.springframework.boot.autoconfigure.session.JdbcSessionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import javax.sql.DataSource;

/**
 * This is {@link CasJdbcSessionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "CasJdbcSessionConfiguration", proxyBeanMethods = false)
@EnableJdbcHttpSession
@EnableConfigurationProperties({CasConfigurationProperties.class, JdbcSessionProperties.class})
@ImportAutoConfiguration(DataSourceAutoConfiguration.class)
public class CasJdbcSessionConfiguration {
    @Bean
    @ConditionalOnMissingBean
    JdbcSessionDataSourceInitializer jdbcSessionDataSourceInitializer(
        @SpringSessionDataSource
        final ObjectProvider<DataSource> sessionDataSource,
        final ObjectProvider<DataSource> dataSource,
        final ResourceLoader resourceLoader,
        final JdbcSessionProperties properties) {
        return new JdbcSessionDataSourceInitializer(sessionDataSource.getIfAvailable(dataSource::getObject), resourceLoader, properties);
    }
}
