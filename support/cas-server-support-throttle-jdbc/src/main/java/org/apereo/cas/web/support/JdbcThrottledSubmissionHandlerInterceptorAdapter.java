package org.apereo.cas.web.support;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;
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
public class JdbcThrottledSubmissionHandlerInterceptorAdapter extends AbstractInspektrAuditHandlerInterceptorAdapter {
    private final DataSource dataSource;
    private final String sqlQueryAudit;
    private final JdbcTemplate jdbcTemplate;

    public JdbcThrottledSubmissionHandlerInterceptorAdapter(final int failureThreshold,
                                                            final int failureRangeInSeconds,
                                                            final String usernameParameter,
                                                            final AuditTrailExecutionPlan auditTrailManager,
                                                            final DataSource dataSource, final String applicationCode,
                                                            final String sqlQueryAudit, final String authenticationFailureCode) {
        super(failureThreshold, failureRangeInSeconds, usernameParameter,
            authenticationFailureCode, auditTrailManager, applicationCode);
        this.dataSource = dataSource;
        this.sqlQueryAudit = sqlQueryAudit;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        final String remoteAddress = clientInfo.getClientIpAddress();

        final List<Timestamp> failuresInAudits = this.jdbcTemplate.query(
            this.sqlQueryAudit,
            new Object[]{
                remoteAddress,
                getUsernameParameterFromRequest(request),
                getAuthenticationFailureCode(),
                getApplicationCode(),
                getFailureInRangeCutOffDate()},
            new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP},
            (resultSet, i) -> resultSet.getTimestamp(1));

        final List<Date> failures = failuresInAudits.stream().map(t -> new Date(t.getTime())).collect(Collectors.toList());
        return calculateFailureThresholdRateAndCompare(failures);
    }

    @Override
    public String getName() {
        return "InspektrIpAddressUsernameThrottle";
    }
}
