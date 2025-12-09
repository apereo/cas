package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnFeaturesEnabled;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.session.jdbc.autoconfigure.JdbcSessionDataSourceScriptDatabaseInitializer;
import org.springframework.boot.session.jdbc.autoconfigure.JdbcSessionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;
import org.springframework.session.jdbc.config.annotation.SpringSessionTransactionManager;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;

/**
 * This is {@link CasJdbcSessionAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ConditionalOnFeaturesEnabled({
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.JDBC),
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SessionManagement)
})
@EnableJdbcHttpSession
@EnableConfigurationProperties({CasConfigurationProperties.class, JdbcSessionProperties.class})
@ImportAutoConfiguration(DataSourceAutoConfiguration.class)
@AutoConfiguration
public class CasJdbcSessionAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    JdbcSessionDataSourceScriptDatabaseInitializer jdbcSessionDataSourceInitializer(
        @SpringSessionDataSource final ObjectProvider<@NonNull DataSource> sessionDataSource,
        final ObjectProvider<@NonNull DataSource> dataSource,
        final JdbcSessionProperties properties) {
        return new JdbcSessionDataSourceScriptDatabaseInitializer(
            sessionDataSource.getIfAvailable(dataSource::getObject), properties);
    }

    @SpringSessionTransactionManager
    @Bean
    @Primary
    public PlatformTransactionManager jdbcSessionTransactionManager(
        final ObjectProvider<@NonNull DataSource> dataSource) {
        val ds = dataSource.getIfAvailable();
        return ds != null ? new DataSourceTransactionManager(ds) : new PseudoTransactionManager();
    }

}
