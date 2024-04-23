package org.apereo.cas.web.report;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller to handle the logging dashboard requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Endpoint(id = "auditLog", enableByDefault = false)
@Slf4j
public class AuditLogEndpoint extends BaseCasActuatorEndpoint {

    private final ObjectProvider<AuditTrailExecutionPlan> auditTrailManager;

    public AuditLogEndpoint(final ObjectProvider<AuditTrailExecutionPlan> auditTrailManager,
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
    @Operation(summary = "Provide a report of the audit log using a given interval",
        parameters = @Parameter(name = "interval", description = "Accepts the duration syntax, such as PT1H"))
    public Set<AuditActionContext> getAuditLog(
        @Selector final String interval) {
        if (StringUtils.isBlank(interval)) {
            val sinceDate = LocalDate.now(ZoneId.systemDefault())
                .minusDays(casProperties.getAudit().getEngine().getNumberOfDaysInHistory());
            return auditTrailManager.getObject().getAuditRecords(Map.of(AuditTrailManager.WhereClauseFields.DATE, sinceDate));
        }

        val duration = Beans.newDuration(interval);
        val sinceTime = LocalDateTime.ofInstant(Instant.now(Clock.systemUTC())
            .minusMillis(duration.toMillis()), ZoneOffset.UTC);
        val days = duration.toDays();
        val sinceDate = LocalDate.now(ZoneOffset.UTC).minusDays(days + 1);
        LOGGER.debug("Fetching audit records since [{}]", sinceDate);
        val initialRecords = auditTrailManager.getObject().getAuditRecords(Map.of(AuditTrailManager.WhereClauseFields.DATE, sinceDate));
        LOGGER.debug("Filtering audit records that are after [{}]", sinceTime);
        return initialRecords
            .stream()
            .filter(rec -> rec.getWhenActionWasPerformed().isAfter(sinceTime))
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
    @WriteOperation(produces = {MEDIA_TYPE_CAS_YAML, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Provide a report of the audit log. Each filter other than `interval` can accept a regular expression to match against.",
        parameters = {
            @Parameter(name = "interval", description = "Accepts the duration syntax, such as PT1H"),
            @Parameter(name = "actionPerformed"),
            @Parameter(name = "clientIpAddress"),
            @Parameter(name = "principal"),
            @Parameter(name = "resourceOperatedUpon")
        })
    public Set<AuditActionContext> getAuditLog(
        @Nullable final String interval,
        @Nullable final String actionPerformed,
        @Nullable final String clientIpAddress,
        @Nullable final String principal,
        @Nullable final String resourceOperatedUpon) {
        return getAuditLog(interval)
            .stream()
            .filter(e -> StringUtils.isBlank(actionPerformed) || e.getActionPerformed().matches(actionPerformed))
            .filter(e -> StringUtils.isBlank(clientIpAddress) || e.getClientInfo().getClientIpAddress().matches(clientIpAddress))
            .filter(e -> StringUtils.isBlank(principal) || e.getPrincipal().matches(principal))
            .filter(e -> StringUtils.isBlank(resourceOperatedUpon) || e.getResourceOperatedUpon().matches(resourceOperatedUpon))
            .collect(Collectors.toSet());
    }
}
