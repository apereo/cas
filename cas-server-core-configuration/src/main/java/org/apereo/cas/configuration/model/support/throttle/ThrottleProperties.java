package org.apereo.cas.configuration.model.support.throttle;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for cas.throttle.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.throttle", ignoreUnknownFields = false)
public class ThrottleProperties {

    private static final String DEFAULT_APPLICATION_CODE = "CAS";

    private static final String DEFAULT_AUTHN_FAILED_ACTION = "AUTHENTICATION_FAILED";

    private static final int DEFAULT_FAILURE_THRESHOLD = 100;

    private static final int DEFAULT_FAILURE_RANGE_IN_SECONDS = 60;

    private static final String DEFAULT_USERNAME_PARAMETER = "username";

    private static final String SQL_AUDIT_QUERY = "SELECT AUD_DATE FROM COM_AUDIT_TRAIL WHERE AUD_CLIENT_IP = ? AND AUD_USER = ? "
            + "AND AUD_ACTION = ? AND APPLIC_CD = ? AND AUD_DATE >= ? ORDER BY AUD_DATE DESC";

    private Failure failure = new Failure();

    private String usernameParameter = DEFAULT_USERNAME_PARAMETER;

    private String appcode = DEFAULT_APPLICATION_CODE;

    private String auditQuery = SQL_AUDIT_QUERY;

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

    public String getAuditQuery() {
        return auditQuery;
    }

    public void setAuditQuery(final String auditQuery) {
        this.auditQuery = auditQuery;
    }

    /**
     * Failure.
     */
    public static class Failure {
        private String code = DEFAULT_AUTHN_FAILED_ACTION;
        private int threshold = DEFAULT_FAILURE_THRESHOLD;
        private int rangeSeconds = DEFAULT_FAILURE_RANGE_IN_SECONDS;

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
}
