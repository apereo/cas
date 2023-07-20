package org.apereo.cas.support.events.audit;

import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;

/**
 * This is {@link CasAuditActionContextRecordedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
public class CasAuditActionContextRecordedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = -1262975970594313844L;

    private final AuditActionContext auditActionContext;

    /**
     * Instantiates a new CAS audit action context recorded event.
     *
     * @param source             the source
     * @param auditActionContext the audit action context
     */
    public CasAuditActionContextRecordedEvent(final Object source, final AuditActionContext auditActionContext, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.auditActionContext = auditActionContext;
    }
}
