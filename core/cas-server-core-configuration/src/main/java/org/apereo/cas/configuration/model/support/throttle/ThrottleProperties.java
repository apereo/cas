package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.Beans;

/**
 * Configuration properties class for cas.throttle.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class ThrottleProperties {

    private static final String DEFAULT_APPLICATION_CODE = "CAS";

    private static final String DEFAULT_AUTHN_FAILED_ACTION = "AUTHENTICATION_FAILED";
        
    private Failure failure = new Failure();
    private Jdbc jdbc = new Jdbc();
    
    private String usernameParameter;
    private String appcode = DEFAULT_APPLICATION_CODE;
    
    private String repeatInterval = "PT20S";
    private String startDelay = "PT10S";

    public Jdbc getJdbc() {
        return jdbc;
    }

    public Failure getFailure() {
        return failure;
    }

    public void setFailure(final Failure failure) {
        this.failure = failure;
    }

    public String getUsernameParameter() {
        return usernameParameter;
    }

    public void setUsernameParameter(final String usernameParameter) {
        this.usernameParameter = usernameParameter;
    }

    public String getAppcode() {
        return appcode;
    }

    public void setAppcode(final String appcode) {
        this.appcode = appcode;
    }
    
    public long getRepeatInterval() {
        return Beans.newDuration(repeatInterval).toMillis();
    }

    public void setRepeatInterval(final String repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public long getStartDelay() {
        return Beans.newDuration(startDelay).toMillis();
    }

    public void setStartDelay(final String startDelay) {
        this.startDelay = startDelay;
    }

    /**
     * Failure.
     */
    public static class Failure {
        private String code = DEFAULT_AUTHN_FAILED_ACTION;
        private int threshold = -1;
        private int rangeSeconds = -1;

        public String getCode() {
            return code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(final int threshold) {
            this.threshold = threshold;
        }

        public int getRangeSeconds() {
            return rangeSeconds;
        }

        public void setRangeSeconds(final int rangeSeconds) {
            this.rangeSeconds = rangeSeconds;
        }
    }

    public static class Jdbc extends AbstractJpaProperties {
        private static final String SQL_AUDIT_QUERY = "SELECT AUD_DATE FROM COM_AUDIT_TRAIL WHERE AUD_CLIENT_IP = ? AND AUD_USER = ? "
                + "AND AUD_ACTION = ? AND APPLIC_CD = ? AND AUD_DATE >= ? ORDER BY AUD_DATE DESC";
        
        private String auditQuery = SQL_AUDIT_QUERY;

        public String getAuditQuery() {
            return auditQuery;
        }

        public void setAuditQuery(final String auditQuery) {
            this.auditQuery = auditQuery;
        }
    }
    
    
}
