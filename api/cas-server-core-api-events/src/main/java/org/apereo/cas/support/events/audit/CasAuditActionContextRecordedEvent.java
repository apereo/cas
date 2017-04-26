package org.apereo.cas.support.events.audit;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.inspektr.audit.AuditActionContext;

/**
 * This is {@link CasAuditActionContextRecordedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasAuditActionContextRecordedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = -1262975970594313844L;
    
    private final AuditActionContext auditActionContext;

    /**
     * Instantiates a new Cas audit action context recorded event.
     *
     * @param source             the source
     * @param auditActionContext the audit action context
     */
    public CasAuditActionContextRecordedEvent(final Object source, final AuditActionContext auditActionContext) {
        super(source);
        this.auditActionContext = auditActionContext;
    }

    public AuditActionContext getAuditActionContext() {
        return this.auditActionContext;
    }
}
