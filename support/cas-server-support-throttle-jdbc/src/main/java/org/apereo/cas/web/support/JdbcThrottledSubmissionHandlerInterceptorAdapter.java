package org.apereo.cas.web.support;

import org.apereo.cas.configuration.model.support.throttle.JdbcThrottleProperties;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Works in conjunction with the Inspektr Library to block attempts to dictionary attack users.
 * <p>
 * Defines a new Inspektr Action "THROTTLED_LOGIN_ATTEMPT" which keeps track of failed login attempts that don't result
 * in AUTHENTICATION_FAILED methods
 * <p>
 * This relies on the default Inspektr table layout and username construction.  The username construction can be overridden
 * in a subclass.
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
@Slf4j
@SuppressWarnings("JavaUtilDate")
public class JdbcThrottledSubmissionHandlerInterceptorAdapter extends AbstractInspektrAuditHandlerInterceptorAdapter {
    private final String sqlQueryAudit;
    private final JdbcTemplate jdbcTemplate;

    public JdbcThrottledSubmissionHandlerInterceptorAdapter(final ThrottledSubmissionHandlerConfigurationContext configurationContext,
                                                            final DataSource dataSource,
                                                            final String sqlQueryAudit) {
        super(configurationContext);
        this.sqlQueryAudit = sqlQueryAudit;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val remoteAddress = clientInfo.getClientIpAddress();

        val username = getUsernameParameterFromRequest(request);
        val failuresInAudits = this.jdbcTemplate.query(
            this.sqlQueryAudit,
            new Object[]{
                remoteAddress,
                username,
                getConfigurationContext().getAuthenticationFailureCode(),
                getConfigurationContext().getApplicationCode(),
                getFailureInRangeCutOffDate()},
            new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP},
            (resultSet, i) -> resultSet.getTimestamp(1));
        val failures = failuresInAudits.stream().map(t -> new Date(t.getTime())).collect(Collectors.toList());
        val result = calculateFailureThresholdRateAndCompare(failures);
        if (result) {
            LOGGER.debug("Request from [{}] by user [{}] exceeds threshold", remoteAddress, username);
        }
        return result;
    }

    @Override
    public String getName() {
        return "InspektrIpAddressUsernameThrottle";
    }

    @Override
    public Collection getRecords() {
        val failuresInAudits = this.jdbcTemplate.query(
            JdbcThrottleProperties.SQL_AUDIT_QUERY_ALL,
            new Object[]{
                getConfigurationContext().getAuthenticationFailureCode(),
                getConfigurationContext().getApplicationCode(),
                getFailureInRangeCutOffDate()},
            new int[]{Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP},
            (resultSet, i) -> resultSet.getTimestamp(1));
        return failuresInAudits.stream().map(t -> new Date(t.getTime())).collect(Collectors.toList());
    }
}
