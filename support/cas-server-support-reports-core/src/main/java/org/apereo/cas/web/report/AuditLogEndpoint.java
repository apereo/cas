package org.apereo.cas.web.report;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller to handle the logging dashboard requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Endpoint(id = "auditLog", enableByDefault = false)
public class AuditLogEndpoint extends BaseCasActuatorEndpoint {

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
        val sinceDate = LocalDate.now().minusDays(casProperties.getAudit().getNumberOfDaysInHistory());
        return this.auditTrailManager.getAuditRecordsSince(sinceDate);
    }

    /**
     * Gets Audit log for passed interval.
     *
     * @param interval - Interval subtracted from current time
     * @return the auditlog
     */
    @ReadOperation
    public Set<AuditActionContext> getAuditLog(final @Selector String interval) {
        val duration = Duration.parse(interval);
        val sinceTime = new Date(new Date().getTime() - duration.toMillis());
        val days = duration.toDays();
        val sinceDate = LocalDate.now().minusDays(days + 1);
        return this.auditTrailManager.getAuditRecordsSince(sinceDate).stream()
                .filter(a -> a.getWhenActionWasPerformed().after(sinceTime))
                .collect(Collectors.toSet());
    }

    /**
     * Gets Audit logs for the passed interval subtracted from current time.  Entries are then filtered to those
     * that match the regular expressions passed in the json body.
     *
     * @param interval - Interval subtracted from current time
     * @param actionPerformed - actionPerformed that was logged
     * @param clientIpAddress - client ip address
     * @param principal - the user id for the log entry
     * @param resourceOperatedUpon - resource operated on.
     * @return - the audit log
     */
    @WriteOperation
    public Set<AuditActionContext> getAuditLog(final String interval,
                                               final String actionPerformed,
                                               final String clientIpAddress,
                                               final String principal,
                                               final String resourceOperatedUpon) {
        return getAuditLog(interval).stream()
                .filter(e-> StringUtils.isNotEmpty(actionPerformed) ? e.getActionPerformed().matches(actionPerformed) : true)
                .filter(e -> StringUtils.isNotEmpty(clientIpAddress) ? e.getClientIpAddress().matches(clientIpAddress) : true)
                .filter(e -> StringUtils.isNotEmpty(principal) ? e.getPrincipal().matches(principal) : true)
                .filter(e -> StringUtils.isNotEmpty(resourceOperatedUpon) ? e.getResourceOperatedUpon().matches(resourceOperatedUpon) : true)
                .collect(Collectors.toSet());
    }
}
