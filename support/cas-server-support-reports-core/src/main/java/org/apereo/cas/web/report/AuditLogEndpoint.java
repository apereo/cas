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
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
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
     * Gets Audit log for passed interval.
     *
     * @param interval - Interval subtracted from current time
     * @return the auditlog
     */
    @ReadOperation
    public Set<AuditActionContext> getAuditLog(@Selector @Nullable final String interval) {
        if (StringUtils.isBlank(interval)) {
            val sinceDate = LocalDate.now(ZoneId.systemDefault()).minusDays(casProperties.getAudit().getNumberOfDaysInHistory());
            return this.auditTrailManager.getAuditRecordsSince(sinceDate);
        }
        
        val duration = Duration.parse(interval);
        val sinceTime = new Date(new Date().getTime() - duration.toMillis());
        val days = duration.toDays();
        val sinceDate = LocalDate.now(ZoneId.systemDefault()).minusDays(days + 1);
        return this.auditTrailManager.getAuditRecordsSince(sinceDate).stream()
            .filter(a -> a.getWhenActionWasPerformed().after(sinceTime))
            .collect(Collectors.toSet());
    }

    /**
     * Gets Audit logs for the passed interval subtracted from current time.  Entries are then filtered to those
     * that match the regular expressions passed in the json body.
     *
     * @param interval             - Interval subtracted from current time
     * @param actionPerformed      - actionPerformed that was logged
     * @param clientIpAddress      - client ip address
     * @param principal            - the user id for the log entry
     * @param resourceOperatedUpon - resource operated on.
     * @return - the audit log
     */
    @WriteOperation
    public Set<AuditActionContext> getAuditLog(final String interval,
                                               final String actionPerformed,
                                               final String clientIpAddress,
                                               final String principal,
                                               final String resourceOperatedUpon) {
        return getAuditLog(interval)
            .stream()
            .filter(e -> StringUtils.isBlank(actionPerformed) || e.getActionPerformed().matches(actionPerformed))
            .filter(e -> StringUtils.isBlank(clientIpAddress) || e.getClientIpAddress().matches(clientIpAddress))
            .filter(e -> StringUtils.isBlank(principal) || e.getPrincipal().matches(principal))
            .filter(e -> StringUtils.isBlank(resourceOperatedUpon) || e.getResourceOperatedUpon().matches(resourceOperatedUpon))
            .collect(Collectors.toSet());
    }
}
