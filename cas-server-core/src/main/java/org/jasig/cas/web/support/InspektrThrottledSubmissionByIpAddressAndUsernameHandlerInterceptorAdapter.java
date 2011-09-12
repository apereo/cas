/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import com.github.inspektr.audit.AuditTrailManager;
import com.github.inspektr.audit.AuditActionContext;
import com.github.inspektr.common.web.ClientInfo;
import com.github.inspektr.common.web.ClientInfoHolder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.Date;
import java.util.Calendar;
import java.sql.Types;

/**
 * Works in conjunction with the Inspektr Library to block attempts to dictionary attack users.
 * <p>
 * Defines a new Inspektr Action "THROTTLED_LOGIN_ATTEMPT" which keeps track of failed login attempts that don't result
 * in AUTHENTICATION_FAILED methods
 * <p>
 * This relies on the default Inspektr table layout and username construction.  The username construction can be overrided
 * in a subclass.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.5
 */
public class InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter extends AbstractThrottledSubmissionHandlerInterceptorAdapter {

    private static final String DEFAULT_APPLICATION_CODE = "CAS";

    private static final String INSPEKTR_ACTION = "THROTTLED_LOGIN_ATTEMPT";

    private final AuditTrailManager auditTrailManager;

    private final JdbcTemplate jdbcTemplate;

    private String applicationCode = DEFAULT_APPLICATION_CODE;

    public InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(final AuditTrailManager auditTrailManager, final DataSource dataSource) {
        this.auditTrailManager = auditTrailManager;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    protected final int findCount(final HttpServletRequest request, final String usernameParameter, final int failureRangeInSeconds) {
        final String SQL = "Select count(*) from COM_AUDIT_TRAIL where AUD_CLIENT_IP = ? and AUD_USER = ? AND AUD_ACTION = ? AND APPLIC_CD = ? AND AUD_DATE >= ?";
        final String userToUse = constructUsername(request, usernameParameter);
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -1 * failureRangeInSeconds);
        final Date oldestDate = calendar.getTime();
        return this.jdbcTemplate.queryForInt(SQL, new Object[] {request.getRemoteAddr(), userToUse, INSPEKTR_ACTION, this.applicationCode, oldestDate}, new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP});
    }

    @Override
    protected final void updateCount(final HttpServletRequest request, final String usernameParameter) {
        final String userToUse = constructUsername(request, usernameParameter);
        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        final AuditActionContext context = new AuditActionContext(userToUse, userToUse, INSPEKTR_ACTION, this.applicationCode, new Date(), clientInfo.getClientIpAddress(), clientInfo.getServerIpAddress());
        this.auditTrailManager.record(context);
    }

    public final void setApplicationCode(final String applicationCode) {
        this.applicationCode = applicationCode;
    }

    protected String constructUsername(HttpServletRequest request, String usernameParameter) {
        final String username = request.getParameter(usernameParameter);
        return "[username: " + (username != null ? username : "") + "]";
    }
}
