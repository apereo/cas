package org.apereo.cas.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

/**
 * Monitors a data source that describes a single connection or connection pool to a database.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class JdbcDataSourceHealthIndicator extends AbstractPoolHealthIndicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcDataSourceHealthIndicator.class);

    private final JdbcTemplate jdbcTemplate;
    private final String validationQuery;

    public JdbcDataSourceHealthIndicator(final int maxWait,
                                         final DataSource dataSource,
                                         final ExecutorService executor,
                                         final String validationQuery) {
        super(maxWait, executor);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.validationQuery = validationQuery;
    }

    @Override
    protected Health.Builder checkPool(final Health.Builder builder) {
        try {
            return this.jdbcTemplate.query(this.validationQuery, rs -> {
                if (rs.next()) {
                    return builder.up();
                }
                return builder.down();
            });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return builder.outOfService().withException(e);
        }
    }
}
