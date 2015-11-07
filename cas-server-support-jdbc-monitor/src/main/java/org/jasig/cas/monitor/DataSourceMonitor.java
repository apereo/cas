package org.jasig.cas.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Monitors a data source that describes a single connection or connection pool to a database.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@Component("dataSourceMonitor")
public class DataSourceMonitor extends AbstractPoolMonitor {

    @NotNull
    private JdbcTemplate jdbcTemplate;

    @NotNull
    @Value("${datasource.monitor.validation.query:SELECT 1}")
    private String validationQuery;

    /**
     * Creates a new instance that monitors the given data source.
     *
     * @param dataSource Data source to monitor.
     */
    @Autowired

    public DataSourceMonitor(@Qualifier("monitorDataSource") @Nullable final DataSource dataSource) {
        if (dataSource != null) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        } else {
            logger.debug("No data source is defined to monitor");
        }
    }


    /**
     * Sets the validation query used to monitor the data source. The validation query should return
     * at least one result; otherwise results are ignored.
     *
     * @param query Validation query that should be as efficient as possible.
     */
    public void setValidationQuery(final String query) {
        this.validationQuery = query;
    }


    @Override
    protected StatusCode checkPool() throws Exception {
        return this.jdbcTemplate.query(this.validationQuery, new ResultSetExtractor<StatusCode>() {
            @Override
            public StatusCode extractData(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return StatusCode.OK;
                }
                return StatusCode.WARN;
            }
        });
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
