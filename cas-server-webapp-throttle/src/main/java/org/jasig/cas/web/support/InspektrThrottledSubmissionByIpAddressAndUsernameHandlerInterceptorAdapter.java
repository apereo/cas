package org.jasig.cas.web.support;

import org.jasig.inspektr.audit.AuditActionContext;
import org.jasig.inspektr.audit.AuditPointRuntimeInfo;
import org.jasig.inspektr.audit.AuditTrailManager;
import org.jasig.inspektr.common.web.ClientInfo;
import org.jasig.inspektr.common.web.ClientInfoHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.List;

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
@Component("inspektrIpAddressUsernameThrottle")
public class InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter
            extends AbstractThrottledSubmissionHandlerInterceptorAdapter {

    private static final String DEFAULT_APPLICATION_CODE = "CAS";

    private static final String DEFAULT_AUTHN_FAILED_ACTION = "AUTHENTICATION_FAILED";

    private static final String INSPEKTR_ACTION = "THROTTLED_LOGIN_ATTEMPT";
    private static final double NUMBER_OF_MILLISECONDS_IN_SECOND = 1000.0;

    private static final String SQL_AUDIT_QUERY = "SELECT AUD_DATE FROM COM_AUDIT_TRAIL WHERE AUD_CLIENT_IP = ? AND AUD_USER = ? "
        + "AND AUD_ACTION = ? AND APPLIC_CD = ? AND AUD_DATE >= ? ORDER BY AUD_DATE DESC";

    @Autowired
    @Qualifier("auditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Nullable
    @Autowired(required=false)
    @Qualifier("inspektrAuditTrailDataSource")
    private DataSource dataSource;

    @Value("${cas.throttle.appcode:" + DEFAULT_APPLICATION_CODE + '}')
    private String applicationCode = DEFAULT_APPLICATION_CODE;

    @Value("${cas.throttle.authn.failurecode:" + DEFAULT_AUTHN_FAILED_ACTION + '}')
    private String authenticationFailureCode = DEFAULT_AUTHN_FAILED_ACTION;

    @Value("${cas.throttle.audit.query:" + SQL_AUDIT_QUERY + '}')
    private String sqlQueryAudit;

    private JdbcTemplate jdbcTemplate;


    /**
     * Instantiates a new Inspektr throttled submission by ip address and username handler interceptor adapter.
     */
    public InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter() {}

    /**
     * Instantiates a new inspektr throttled submission by ip address and username handler interceptor adapter.
     *
     * @param auditTrailManager the audit trail manager
     * @param dataSource the data source
     */
    public InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(final AuditTrailManager auditTrailManager,
            final DataSource dataSource) {
        this.auditTrailManager = auditTrailManager;
        init();
    }

    /**
     * Init the jdbc template.
     */
    @PostConstruct
    public void init() {

        if (this.dataSource != null) {
            this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        } else {
            logger.debug("No data source is defined for {}. Ignoring the construction of JDBC template",
                    this.getName());
        }
    }

    @Override
    protected boolean exceedsThreshold(final HttpServletRequest request) {
        if (this.dataSource != null && this.jdbcTemplate != null) {
            final String userToUse = constructUsername(request, getUsernameParameter());
            final Calendar cutoff = Calendar.getInstance();
            cutoff.add(Calendar.SECOND, -1 * getFailureRangeInSeconds());

            final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
            final String remoteAddress = clientInfo.getClientIpAddress();

            final List<Timestamp> failures = this.jdbcTemplate.query(
                    sqlQueryAudit,
                    new Object[]{remoteAddress, userToUse, this.authenticationFailureCode,
                            this.applicationCode, cutoff.getTime()},
                    new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP},
                    new RowMapper<Timestamp>() {
                        @Override
                        public Timestamp mapRow(final ResultSet resultSet, final int i) throws SQLException {
                            return resultSet.getTimestamp(1);
                        }
                    });
            if (failures.size() < 2) {
                return false;
            }
            // Compute rate in submissions/sec between last two authn failures and compare with threshold
            return NUMBER_OF_MILLISECONDS_IN_SECOND / (failures.get(0).getTime() - failures.get(1).getTime()) > getThresholdRate();
        }
        logger.debug("No data source is defined for {}. Ignoring threshold checking",
                this.getName());
        return false;
    }

    @Override
    protected void recordSubmissionFailure(final HttpServletRequest request) {
        recordThrottle(request);
    }

    @Override
    protected void recordThrottle(final HttpServletRequest request) {
        if (this.dataSource != null && this.jdbcTemplate != null) {
            super.recordThrottle(request);
            final String userToUse = constructUsername(request, getUsernameParameter());
            final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
            final AuditPointRuntimeInfo auditPointRuntimeInfo = new AuditPointRuntimeInfo() {
                private static final long serialVersionUID = 1L;

                @Override
                public String asString() {
                    return String.format("%s.recordThrottle()", this.getClass().getName());
                }
            };
            final AuditActionContext context = new AuditActionContext(
                    userToUse,
                    userToUse,
                    INSPEKTR_ACTION,
                    this.applicationCode,
                    new java.util.Date(),
                    clientInfo.getClientIpAddress(),
                    clientInfo.getServerIpAddress(),
                    auditPointRuntimeInfo);
            this.auditTrailManager.record(context);
        } else {
            logger.debug("No data source is defined for {}. Ignoring audit record-keeping",
                    this.getName());
        }
    }

    public final void setApplicationCode(final String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public final void setAuthenticationFailureCode(final String authenticationFailureCode) {
        this.authenticationFailureCode = authenticationFailureCode;
    }

    /**
     * Construct username from the request.
     *
     * @param request the request
     * @param usernameParameter the username parameter
     * @return the string
     */
    protected String constructUsername(final HttpServletRequest request, final String usernameParameter) {
        return request.getParameter(usernameParameter);
    }

    @Override
    protected String getName() {
        return "inspektrIpAddressUsernameThrottle";
    }
}
