package org.apereo.cas.monitor;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;

/**
 * Monitors a data source that describes a single connection or connection pool to a database.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class JdbcDataSourceMonitor extends AbstractPoolMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcDataSourceMonitor.class);
    
    private JdbcTemplate jdbcTemplate;
    private final String validationQuery;
            
    /**
     * Creates a new instance that monitors the given data source.
     *
     * @param executorService the executor service
     * @param maxWait         the max wait
     * @param dataSource      Data source to monitor.
     * @param validationQuery validation query used to monitor the data source. The validation query
     *                        should return at least one result; otherwise results are ignored.
     */
    public JdbcDataSourceMonitor(final ExecutorService executorService, final int maxWait,
                                 final DataSource dataSource, final String validationQuery) {
        super(JdbcDataSourceMonitor.class.getSimpleName(), executorService, maxWait);
        if (dataSource != null) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        } else {
            LOGGER.debug("No data source is defined to monitor");
        }
        this.validationQuery = validationQuery;
    }

    @Override
    protected StatusCode checkPool() throws Exception {
        try {
            return this.jdbcTemplate.query(this.validationQuery, (ResultSet rs) -> {
                if (rs.next()) {
                    return StatusCode.OK;
                }
                return StatusCode.WARN;
            });
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected int getIdleCount() {
        return PoolStatus.UNKNOWN_COUNT;
    }

    @Override
    protected int getActiveCount() {
        return PoolStatus.UNKNOWN_COUNT;
    }
}
