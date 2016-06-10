package org.apereo.cas.configuration.model.core.audit;

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
    
    private boolean useSingleLine;
    
    private AbstractStringAuditTrailManager.AuditFormats auditFormat =
            AbstractStringAuditTrailManager.AuditFormats.DEFAULT;
    
    private boolean ignoreAuditFailures;

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
}
