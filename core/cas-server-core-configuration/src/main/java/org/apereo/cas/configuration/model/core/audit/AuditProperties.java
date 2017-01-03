package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.inspektr.audit.support.AbstractStringAuditTrailManager;

/**
 * This is {@link AuditProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuditProperties {

    private String appCode = "CAS";
    private String singlelineSeparator = "|";
    
    private String alternateServerAddrHeaderName;
    private String alternateClientAddrHeaderName;
    private boolean useServerHostAddress;
    
    private boolean useSingleLine;

    private Jdbc jdbc = new Jdbc();

    private AbstractStringAuditTrailManager.AuditFormats auditFormat =
            AbstractStringAuditTrailManager.AuditFormats.DEFAULT;

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
        private int maxAgeDays = 180;

        private String isolationLevelName = "ISOLATION_READ_COMMITTED";
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
