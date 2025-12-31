package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This is {@link CasJdbcCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "jdbc")
@AutoConfiguration
public class CasJdbcCloudConfigBootstrapAutoConfiguration {

    /**
     * The CAS Jdbc configuration key prefix.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.jdbc";

    @Bean
    @ConditionalOnMissingBean(name = "jdbcPropertySourceLocator")
    public PropertySourceLocator jdbcPropertySourceLocator(
        @Qualifier("jdbcCloudConfigurationTemplate") final JdbcTemplate jdbcTemplate) {
        return new JdbcPropertySourceLocator(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(name = "jdbcCloudConfigurationTemplate")
    public JdbcTemplate jdbcCloudConfigurationTemplate(final ConfigurableEnvironment environment) {
        val connection = new JdbcCloudConnection(environment);
        val dataSource = JpaBeans.newDataSource(connection.setAutocommit(true));
        return new JdbcTemplate(dataSource);
    }

    @RequiredArgsConstructor
    private static final class JdbcCloudConnection extends AbstractJpaProperties {
        @Serial
        private static final long serialVersionUID = 3141915452108685020L;

        private final Environment environment;

        private static @Nullable String getSetting(final Environment environment, final String key) {
            return environment.getProperty(CAS_CONFIGURATION_PREFIX + '.' + key);
        }

        @Override
        public @Nullable String getUrl() {
            return StringUtils.defaultIfBlank(getSetting(environment, "url"), super.getUrl());
        }

        @Override
        public @Nullable String getPassword() {
            return StringUtils.defaultIfBlank(getSetting(environment, "password"), super.getPassword());
        }

        @Override
        public @Nullable String getUser() {
            return StringUtils.defaultIfBlank(getSetting(environment, "user"), super.getUser());
        }

        @Override
        public @Nullable String getDriverClass() {
            return StringUtils.defaultIfBlank(getSetting(environment, "driver-class"), super.getDriverClass());
        }
    }
}
