package org.apereo.inspektr.audit.support;

import module java.base;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

/**
 * {@link AuditTrailManager} that dumps auditable information to a configured logger.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0
 */
public class Slf4jLoggingAuditTrailManager extends AbstractStringAuditTrailManager {
    @Override
    public void record(final AuditActionContext auditActionContext) {
        LOG.info(toString(auditActionContext));
    }
}
