package org.apereo.cas.web.report;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.spi.AuditActionDateProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Controller to handle the logging dashboard requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Endpoint(id = "auditLog", defaultAccess = Access.NONE)
@Slf4j
public class AuditLogEndpoint extends BaseCasRestActuatorEndpoint {

    private final ObjectProvider<@NonNull AuditTrailExecutionPlan> auditTrailManager;
    private final ObjectProvider<@NonNull AuditActionDateProvider> auditActionDateProvider;

    public AuditLogEndpoint(final ObjectProvider<@NonNull AuditTrailExecutionPlan> auditTrailManager,
                            final ConfigurableApplicationContext applicationContext,
                            final ObjectProvider<@NonNull AuditActionDateProvider> auditActionDateProvider,
                            final CasConfigurationProperties casProperties) {
        super(casProperties, applicationContext);
        this.auditActionDateProvider = auditActionDateProvider;
        this.auditTrailManager = auditTrailManager;
    }

    private Stream<AuditActionContext> getAuditLog(final String interval, final long count) {
        if (StringUtils.isBlank(interval)) {
            val sinceDate = auditActionDateProvider.getObject().get()
                .minusDays(casProperties.getAudit().getEngine().getNumberOfDaysInHistory());
            return auditTrailManager.getObject().getAuditRecords(Map.of(
                AuditTrailManager.WhereClauseFields.DATE, sinceDate,
                AuditTrailManager.WhereClauseFields.COUNT, count
            )).parallelStream();
        }
        val duration = Beans.newDuration(interval);
        val startingDate = LocalDateTime.from(duration.subtractFrom(auditActionDateProvider.getObject().get()));
        LOGGER.debug("Fetching audit records since [{}]", startingDate);
        val initialRecords = auditTrailManager.getObject().getAuditRecords(Map.of(AuditTrailManager.WhereClauseFields.DATE, startingDate));
        LOGGER.debug("Filtering audit records that are after [{}]", startingDate);
        return initialRecords
            .parallelStream()
            .filter(rec -> rec.getWhenActionWasPerformed().isAfter(startingDate));
    }

    /**
     * Gets audit log.
     *
     * @param count                the count
     * @param interval             the interval
     * @param actionPerformed      the action performed
     * @param clientIpAddress      the client ip address
     * @param principal            the principal
     * @param resourceOperatedUpon the resource operated upon
     * @return the audit log
     */
    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE
        })
    @Operation(summary = "Provide a report of the audit log. Each filter other than `interval` can accept a regular expression to match against.",
        parameters = {
            @Parameter(name = "count", description = "Total number of records to fetch from audit log"),
            @Parameter(name = "interval", description = "Accepts the duration syntax, such as PT1H"),
            @Parameter(name = "actionPerformed", description = "The action performed"),
            @Parameter(name = "clientIpAddress", description = "The client IP address"),
            @Parameter(name = "principal", description = "The principal"),
            @Parameter(name = "resourceOperatedUpon", description = "The resource operated upon")
        })
    public List<AuditActionContext> getAuditLog(
        @RequestParam(required = false, defaultValue = "10") final int count,
        @RequestParam(required = false) final String interval,
        @RequestParam(required = false) final String actionPerformed,
        @RequestParam(required = false) final String clientIpAddress,
        @RequestParam(required = false) final String principal,
        @RequestParam(required = false) final String resourceOperatedUpon) {
        val records = getAuditLog(interval, count)
            .filter(e -> StringUtils.isBlank(actionPerformed) || RegexUtils.find(actionPerformed, e.getActionPerformed()))
            .filter(e -> StringUtils.isBlank(clientIpAddress) || RegexUtils.find(clientIpAddress, e.getClientInfo().getClientIpAddress()))
            .filter(e -> StringUtils.isBlank(principal) || RegexUtils.find(principal, e.getPrincipal()))
            .filter(e -> StringUtils.isBlank(resourceOperatedUpon) || RegexUtils.find(resourceOperatedUpon, e.getResourceOperatedUpon()))
            .toList();
        LOGGER.debug("Found [{}] audit log records", records.size());
        return records;
    }
}
