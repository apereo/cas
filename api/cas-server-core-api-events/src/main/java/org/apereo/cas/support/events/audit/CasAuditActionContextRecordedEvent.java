package org.apereo.cas.support.events.audit;

import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditActionContext;

/**
 * This is {@link CasAuditActionContextRecordedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
public class CasAuditActionContextRecordedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = -1262975970594313844L;

    private final AuditActionContext auditActionContext;

    /**
     * Instantiates a new CAS audit action context recorded event.
     *
     * @param source             the source
     * @param auditActionContext the audit action context
     */
    public CasAuditActionContextRecordedEvent(final Object source, final AuditActionContext auditActionContext) {
        super(source);
        this.auditActionContext = auditActionContext;
    }
}
