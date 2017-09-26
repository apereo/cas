package org.apereo.cas.configuration.model.core.audit;

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
     * {@code X-Forwarded-For} to glean the client address
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
    private AuditJdbcProperties jdbc = new AuditJdbcProperties();

    /**
     * Family of sub-properties pertaining to MongoDb-based audit destinations.
     */
    private AuditMongoDbProperties mongo = new AuditMongoDbProperties();

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

    public AuditMongoDbProperties getMongo() {
        return mongo;
    }

    public void setMongo(final AuditMongoDbProperties mongo) {
        this.mongo = mongo;
    }

    public AuditJdbcProperties getJdbc() {
        return jdbc;
    }

    public void setJdbc(final AuditJdbcProperties jdbc) {
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

}
