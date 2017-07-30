package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.inspektr.audit.support.AbstractStringAuditTrailManager;

import java.io.Serializable;

/**
 * This is {@link AuditProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuditProperties implements Serializable {

    private static final long serialVersionUID = 3946106584608417663L;
    /**
     * Application code to use in the audit logs.
     *
     * This is a unique code that acts as the identifier for the application.
     * In case audit logs are aggregated in a central location. This makes it easy
     * to identify the application and filter results based on the code.
     */
    private String appCode = "CAS";

    /**
     * Character to separate audit fields if single-line audits are used.
     */
    private String singlelineSeparator = "|";

    /**
     * Request header to use identify the server address.
     */
    private String alternateServerAddrHeaderName;

    /**
     * Request header to use identify the client address.
     *
     * If the application is sitting behind a load balancer,
     * the client address typically ends up being the load balancer
     * address itself. A common example for a header here would be
     * <code>X-Forwarded-For</code> to glean the client address
     * from the request, assuming the load balancer is configured correctly
     * to pass that header along.
     */
    private String alternateClientAddrHeaderName;

    /**
     * Determines whether a local DNS lookup should be made to query for the CAS server address.
     *
     * By default, the server is address is determined from the request. Aside from special headers,
     * this option allows one to query DNS to look up the server address of the CAS server processing requests.
     */
    private boolean useServerHostAddress;

    /**
     * Indicates whether audit logs should be recorded as a single-line.
     *
     * By default, audit logs are split into multiple lines where each action and activity
     * takes up a full line. This is a more compact version.
     */
    private boolean useSingleLine;

    /**
     * Family of sub-properties pertaining to Jdbc-based audit destinations.
     */
    private Jdbc jdbc = new Jdbc();

    /**
     * The audit format to use in the logs.
     */
    private AbstractStringAuditTrailManager.AuditFormats auditFormat =
            AbstractStringAuditTrailManager.AuditFormats.DEFAULT;

    /**
     * Indicates whether catastrophic audit failures should simply be logged
     * or whether errors should bubble up and thrown back.
     */
    private boolean ignoreAuditFailures;

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(final Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(final String appCode) {
        this.appCode = appCode;
    }

    public String getSinglelineSeparator() {
        return singlelineSeparator;
    }

    public void setSinglelineSeparator(final String singlelineSeparator) {
        this.singlelineSeparator = singlelineSeparator;
    }

    public boolean isUseSingleLine() {
        return useSingleLine;
    }

    public void setUseSingleLine(final boolean useSingleLine) {
        this.useSingleLine = useSingleLine;
    }

    public AbstractStringAuditTrailManager.AuditFormats getAuditFormat() {
        return auditFormat;
    }

    public void setAuditFormat(final AbstractStringAuditTrailManager.AuditFormats auditFormat) {
        this.auditFormat = auditFormat;
    }

    public boolean isIgnoreAuditFailures() {
        return ignoreAuditFailures;
    }

    public void setIgnoreAuditFailures(final boolean ignoreAuditFailures) {
        this.ignoreAuditFailures = ignoreAuditFailures;
    }

    public String getAlternateServerAddrHeaderName() {
        return alternateServerAddrHeaderName;
    }

    public void setAlternateServerAddrHeaderName(final String alternateServerAddrHeaderName) {
        this.alternateServerAddrHeaderName = alternateServerAddrHeaderName;
    }

    public String getAlternateClientAddrHeaderName() {
        return alternateClientAddrHeaderName;
    }

    public void setAlternateClientAddrHeaderName(final String alternateClientAddrHeaderName) {
        this.alternateClientAddrHeaderName = alternateClientAddrHeaderName;
    }

    public boolean isUseServerHostAddress() {
        return useServerHostAddress;
    }

    public void setUseServerHostAddress(final boolean useServerHostAddress) {
        this.useServerHostAddress = useServerHostAddress;
    }

    public static class Jdbc extends AbstractJpaProperties {
        private static final long serialVersionUID = 4227475246873515918L;

        /**
         * Indicates how long audit records should be kept in the database.
         * This is used by the clean-up criteria to clean up after stale audit records.
         */
        private int maxAgeDays = 180;

        /**
         * Defines the isolation level for transactions.
         * @see org.springframework.transaction.TransactionDefinition
         */
        private String isolationLevelName = "ISOLATION_READ_COMMITTED";

        /**
         * Defines the propagation behavior for transactions.
         * @see org.springframework.transaction.TransactionDefinition
         */
        private String propagationBehaviorName = "PROPAGATION_REQUIRED";

        public int getMaxAgeDays() {
            return maxAgeDays;
        }

        public void setMaxAgeDays(final int maxAgeDays) {
            this.maxAgeDays = maxAgeDays;
        }

        public String getPropagationBehaviorName() {
            return propagationBehaviorName;
        }

        public void setPropagationBehaviorName(final String propagationBehaviorName) {
            this.propagationBehaviorName = propagationBehaviorName;
        }

        public String getIsolationLevelName() {
            return isolationLevelName;
        }

        public void setIsolationLevelName(final String isolationLevelName) {
            this.isolationLevelName = isolationLevelName;
        }
    }
}
