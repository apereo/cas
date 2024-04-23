package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link JdbcThrottleProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-throttle-jdbc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("JdbcThrottleProperties")
public class JdbcThrottleProperties extends AbstractJpaProperties {
    /**
     * SQL throttling query for all failing records.
     */
    public static final String SQL_AUDIT_QUERY_ALL = "SELECT * FROM COM_AUDIT_TRAIL WHERE "
                                                     + "AUD_ACTION = ? AND APPLIC_CD = ? AND AUD_DATE >= ? ORDER BY AUD_DATE DESC";

    /**
     * SQL throttling query.
     */
    private static final String SQL_AUDIT_QUERY_BY_USER_AND_IP = "SELECT * FROM COM_AUDIT_TRAIL "
                                                                 + "WHERE AUD_CLIENT_IP = ? AND AUD_USER = ? "
                                                                 + "AND AUD_ACTION = ? AND APPLIC_CD = ? AND AUD_DATE >= ? "
                                                                 + "ORDER BY AUD_DATE DESC";

    @Serial
    private static final long serialVersionUID = -9199878384425691919L;

    /**
     * Decide whether JDBC audits should be enabled.
     */
    private boolean enabled = true;

    /**
     * Audit query to execute against the database
     * to locate audit records based on IP, user, date and
     * an application code along with the relevant audit action.
     */
    private String auditQuery = SQL_AUDIT_QUERY_BY_USER_AND_IP;

}
