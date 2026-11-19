package org.apereo.cas.web.report;

import module java.base;
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
import org.apereo.inspektr.audit.AuditActionCount;
import org.apereo.inspektr.audit.AuditSummaryQuery;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.spi.AuditActionDateProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller to handle the logging dashboard requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Endpoint(id = "auditLog", defaultAccess = Access.NONE)
@Slf4j
public class AuditLogEndpoint extends BaseCasRestActuatorEndpoint {
    /**
     * The actions counted when none are asked for: sign-ins that succeed or fail, sign-ins refused by throttling, and
     * multifactor authentication being triggered.
     */
    public static final String DEFAULT_SUMMARY_ACTIONS =
        "AUTHENTICATION_SUCCESS,AUTHENTICATION_FAILED,THROTTLED_LOGIN_ATTEMPT,AUTHENTICATION_EVENT_TRIGGERED";

    private static final Duration MAX_SUMMARY_PERIOD = Duration.ofDays(7);

    private static final Duration SUMMARY_KEPT_FOR = Duration.ofMinutes(1);

    private final ObjectProvider<AuditTrailExecutionPlan> auditTrailManager;
    private final ObjectProvider<AuditActionDateProvider> auditActionDateProvider;

    private final Map<AuditSummaryQuery, AuditSummary> summaries = new ConcurrentHashMap<>();

    public AuditLogEndpoint(final ObjectProvider<AuditTrailExecutionPlan> auditTrailManager,
                            final ConfigurableApplicationContext applicationContext,
                            final ObjectProvider<AuditActionDateProvider> auditActionDateProvider,
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

    /**
     * Count audit records per action and per hour, and per target when asked, as the audit storage counts them. Each
     * answer is kept for a minute, so viewers asking for the same period share one count.
     *
     * @param interval the period up to the current hour, at most a week
     * @param actions  the actions to count
     * @param groupBy  {@code target} to count per target too
     * @return the summary
     */
    @GetMapping(path = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Count audit records per action and per hour, when the audit storage can count them.",
        parameters = {
            @Parameter(name = "interval", description = "Accepts the duration syntax, such as PT24H; at most a week"),
            @Parameter(name = "actions", description = "The actions to count"),
            @Parameter(name = "groupBy", description = "target, to count per target too: what each action was for")
        })
    public AuditSummary getAuditSummary(
        @RequestParam(required = false, defaultValue = "PT24H") final String interval,
        @RequestParam(required = false, defaultValue = DEFAULT_SUMMARY_ACTIONS) final List<String> actions,
        @RequestParam(required = false, defaultValue = StringUtils.EMPTY) final String groupBy) {
        val period = Beans.newDuration(interval);
        val length = period.isNegative() || period.isZero() || period.compareTo(MAX_SUMMARY_PERIOD) > 0 ? MAX_SUMMARY_PERIOD : period;
        val now = auditActionDateProvider.getObject().get();
        val to = AuditSummaryQuery.hourOf(now).plus(AuditSummaryQuery.BUCKET);
        val query = new AuditSummaryQuery(to.minus(length), to, new LinkedHashSet<>(actions), "target".equalsIgnoreCase(groupBy));
        val expired = Instant.now().minus(SUMMARY_KEPT_FOR);
        summaries.values().removeIf(summary -> summary.generated().isBefore(expired));
        val cached = summaries.get(query);
        if (cached != null) {
            return cached;
        }
        val counts = auditTrailManager.getObject().summarize(query);
        val summary = new AuditSummary(query, counts.isPresent(), counts.orElseGet(List::of), Instant.now());
        summaries.put(query, summary);
        return summary;
    }

    /**
     * Counts of audit records per action and per hour.
     *
     * @param query     the period and actions counted
     * @param supported whether the audit storage can count its records; when not, counts is empty
     * @param counts    the counts, hours and actions without records left out
     * @param generated when the counts were made
     */
    public record AuditSummary(AuditSummaryQuery query, boolean supported, List<AuditActionCount> counts, Instant generated) {
    }
}
