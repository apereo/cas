package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link JdbcCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("jdbcCloudConfigBootstrapConfiguration")
public class JdbcCloudConfigBootstrapConfiguration implements PropertySourceLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcCloudConfigBootstrapConfiguration.class);

    @Override
    public PropertySource<?> locate(final Environment environment) {
        final Properties props = new Properties();

        try {
            final JdbcCloudConnection connection = new JdbcCloudConnection(environment);
            final DataSource dataSource = JpaBeans.newDataSource(connection);
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            final List<Map<String, Object>> rows = jdbcTemplate.queryForList(connection.getSql());
            for (final Map row : rows) {
                props.put(row.get("name"), row.get("value"));
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }

    private static class JdbcCloudConnection extends AbstractJpaProperties {
        private static final String SQL = "SELECT id, name, value FROM CAS_SETTINGS_TABLE";
        private static final long serialVersionUID = 3141915452108685020L;

        private final Environment environment;

        JdbcCloudConnection(final Environment environment) {
            this.environment = environment;
        }

        private static String getSetting(final Environment environment, final String key) {
            return environment.getProperty("cas.spring.cloud.jdbc." + key);
        }

        public String getSql() {
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
            return StringUtils.defaultIfBlank(getSetting(environment, "driverClass"), super.getDriverClass());
        }
    }
}
