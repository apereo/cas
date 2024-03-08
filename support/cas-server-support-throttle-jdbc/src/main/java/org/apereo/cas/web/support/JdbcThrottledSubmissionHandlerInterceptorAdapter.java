package org.apereo.cas.web.support;

import org.apereo.cas.configuration.model.support.throttle.JdbcThrottleProperties;
import org.apereo.cas.throttle.AbstractInspektrAuditHandlerInterceptorAdapter;
import org.apereo.cas.throttle.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.jdbc.core.JdbcOperations;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
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
    private final JdbcOperations jdbcTemplate;

    public JdbcThrottledSubmissionHandlerInterceptorAdapter(
        final ThrottledSubmissionHandlerConfigurationContext configurationContext,
        final JdbcOperations jdbcTemplate) {
        super(configurationContext);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle();
        val clientInfo = ClientInfoHolder.getClientInfo();
        val remoteAddress = clientInfo.getClientIpAddress();
        val username = getUsernameParameterFromRequest(request);

        LOGGER.debug("Fetching failures in audit log for username [{}] and remote address [{}]", username, remoteAddress);
        val failuresInAudits = jdbcTemplate.query(
            throttle.getJdbc().getAuditQuery(),
            ps -> {
                ps.setString(1, remoteAddress);
                ps.setString(2, username);
                ps.setString(3, throttle.getFailure().getCode());
                ps.setString(4, throttle.getCore().getAppCode());
                ps.setObject(5, getFailureInRangeCutOffDate());
            },
            (resultSet, i) -> ThrottledSubmission
                .builder()
                .key(UUID.randomUUID().toString())
                .value(DateTimeUtils.zonedDateTimeOf(resultSet.getTimestamp("AUD_DATE")))
                .build());
        LOGGER.debug("Found [{}] failure(s) in audit log", failuresInAudits.size());
        val result = calculateFailureThresholdRateAndCompare(failuresInAudits);
        if (result) {
            LOGGER.debug("Request from [{}] by user [{}] exceeds threshold", remoteAddress, username);
        }
        return result;
    }

    @Override
    public String getName() {
        return "JdbcThrottle";
    }

    @Override
    public Collection getRecords() {
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle();
        val failuresInAudits = jdbcTemplate.query(
            JdbcThrottleProperties.SQL_AUDIT_QUERY_ALL,
            new Object[]{
                throttle.getFailure().getCode(),
                throttle.getCore().getAppCode(),
                getFailureInRangeCutOffDate()},
            new int[]{Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP},
            (resultSet, i) -> resultSet.getTimestamp("AUD_DATE"));
        return failuresInAudits.stream().map(t -> new Date(t.getTime())).collect(Collectors.toList());
    }
}
