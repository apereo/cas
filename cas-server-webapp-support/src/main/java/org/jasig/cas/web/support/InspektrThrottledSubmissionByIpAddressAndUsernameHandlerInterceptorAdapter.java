/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web.support;

import org.jasig.inspektr.audit.AuditActionContext;
import org.jasig.inspektr.audit.AuditPointRuntimeInfo;
import org.jasig.inspektr.audit.AuditTrailManager;
import org.jasig.inspektr.common.web.ClientInfo;
import org.jasig.inspektr.common.web.ClientInfoHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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
 * This relies on the default Inspektr table layout and username construction.  The username construction can be overriden
 * in a subclass.
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
public class InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter
            extends AbstractThrottledSubmissionHandlerInterceptorAdapter {

    private static final String DEFAULT_APPLICATION_CODE = "CAS";

    private static final String DEFAULT_AUTHN_FAILED_ACTION = "AUTHENTICATION_FAILED";

    private static final String INSPEKTR_ACTION = "THROTTLED_LOGIN_ATTEMPT";
    private static final double NUMBER_OF_MILLISECONDS_IN_SECOND = 1000.0;

    private final AuditTrailManager auditTrailManager;

    private final JdbcTemplate jdbcTemplate;

    private String applicationCode = DEFAULT_APPLICATION_CODE;

    private String authenticationFailureCode = DEFAULT_AUTHN_FAILED_ACTION;

    /**
     * Instantiates a new inspektr throttled submission by ip address and username handler interceptor adapter.
     *
     * @param auditTrailManager the audit trail manager
     * @param dataSource the data source
     */
    public InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(final AuditTrailManager auditTrailManager,
            final DataSource dataSource) {
        this.auditTrailManager = auditTrailManager;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    protected boolean exceedsThreshold(final HttpServletRequest request) {
        final String query = "SELECT AUD_DATE FROM COM_AUDIT_TRAIL WHERE AUD_CLIENT_IP = ? AND AUD_USER = ? "
                + "AND AUD_ACTION = ? AND APPLIC_CD = ? AND AUD_DATE >= ? ORDER BY AUD_DATE DESC";
        final String userToUse = constructUsername(request, getUsernameParameter());
        final Calendar cutoff = Calendar.getInstance();
        cutoff.add(Calendar.SECOND, -1 * getFailureRangeInSeconds());
        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        final String remoteAddress = clientInfo.getClientIpAddress();
        final List<Timestamp> failures = this.jdbcTemplate.query(
                query,
                new Object[] {remoteAddress, userToUse, this.authenticationFailureCode, this.applicationCode, cutoff.getTime()},
                new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP},
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

    @Override
    protected void recordSubmissionFailure(final HttpServletRequest request) {
        // No internal counters to update
    }

    @Override
    protected void recordThrottle(final HttpServletRequest request) {
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
}
