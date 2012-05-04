/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import com.github.inspektr.audit.AuditPointRuntimeInfo;
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
        final AuditPointRuntimeInfo auditPointRuntimeInfo = new AuditPointRuntimeInfo() {
            public String asString() {
                return String.format("%s.updateCount()", this.getClass().getName());
            }
        };
        final AuditActionContext context = new AuditActionContext(userToUse, userToUse, INSPEKTR_ACTION, this.applicationCode, new Date(), clientInfo.getClientIpAddress(), clientInfo.getServerIpAddress(), auditPointRuntimeInfo);
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
