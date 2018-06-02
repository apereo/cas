package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.apereo.inspektr.audit.AuditActionContext;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.time.LocalDate;
import java.util.Set;

/**
 * Controller to handle the logging dashboard requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Endpoint(id = "audit-log", enableByDefault = false)
public class AuditLogEndpoint extends BaseCasMvcEndpoint {

    private final AuditTrailExecutionPlan auditTrailManager;

    public AuditLogEndpoint(final AuditTrailExecutionPlan auditTrailManager,
                            final CasConfigurationProperties casProperties) {
        super(casProperties);
        this.auditTrailManager = auditTrailManager;
    }


    /**
     * Gets audit log.
     *
     * @return the audit log
     */
    @ReadOperation
    public Set<AuditActionContext> getAuditLog() {
        final var sinceDate = LocalDate.now().minusDays(getCasProperties().getAudit().getNumberOfDaysInHistory());
        return this.auditTrailManager.getAuditRecordsSince(sinceDate);
    }
}
