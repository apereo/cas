package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serial;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This is {@link CasJdbcCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "jdbc")
@AutoConfiguration
public class CasJdbcCloudConfigBootstrapAutoConfiguration implements PropertySourceLocator {

    private static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.jdbc";

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val props = new Properties();

        try {
            val connection = new JdbcCloudConnection(environment);
            val dataSource = JpaBeans.newDataSource(connection);
            val jdbcTemplate = new JdbcTemplate(dataSource);
            val rows = jdbcTemplate.queryForList(connection.getSql());
            props.putAll(rows
                .stream()
                .filter(row -> row.get("value") != null)
                .collect(Collectors.toMap(row -> row.get("name"), row -> row.get("value"), (a, b) -> b, Properties::new)));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }

    private static final class JdbcCloudConnection extends AbstractJpaProperties {
        private static final String SQL = "SELECT id, name, value FROM CAS_SETTINGS_TABLE";
        @Serial
        private static final long serialVersionUID = 3141915452108685020L;

        private final transient Environment environment;

        JdbcCloudConnection(final Environment environment) {
            this.environment = environment;
        }

        private static @Nullable String getSetting(final Environment environment, final String key) {
            return environment.getProperty(CAS_CONFIGURATION_PREFIX + '.' + key);
        }

        String getSql() {
            return StringUtils.defaultIfBlank(getSetting(environment, "sql"), SQL);
        }

        @Override
        public String getUrl() {
            return StringUtils.defaultIfBlank(getSetting(environment, "url"), super.getUrl());
        }

        @Override
        public String getPassword() {
            return StringUtils.defaultIfBlank(getSetting(environment, "password"), super.getPassword());
        }

        @Override
        public String getUser() {
            return StringUtils.defaultIfBlank(getSetting(environment, "user"), super.getUser());
        }

        @Override
        public String getDriverClass() {
            return StringUtils.defaultIfBlank(getSetting(environment, "driver-class"), super.getDriverClass());
        }
    }
}
