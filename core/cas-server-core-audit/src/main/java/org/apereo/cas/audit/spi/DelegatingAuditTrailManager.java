package org.apereo.cas.audit.spi;

import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

import java.util.Set;

/**
 * This is {@link DelegatingAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface DelegatingAuditTrailManager extends AuditTrailManager {

    /**
     * Get set of audit records.
     *
     * @return the records
     */
    Set<AuditActionContext> get();
}
