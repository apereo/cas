package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.apereo.inspektr.audit.AuditActionContext;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Scott Battaglia
 * @since 3.3.5
 */
@Slf4j
@Endpoint(id = "statistics", enableByDefault = false)
public class StatisticsEndpoint extends BaseCasMvcEndpoint {
    private static final int NUMBER_OF_BYTES_IN_A_KILOBYTE = 1024;

    private final ZonedDateTime upTimeStartDate = ZonedDateTime.now(ZoneOffset.UTC);

    private final AuditTrailExecutionPlan auditTrailManager;
    private final CentralAuthenticationService centralAuthenticationService;

    public StatisticsEndpoint(final AuditTrailExecutionPlan auditTrailManager,
                              final CentralAuthenticationService centralAuthenticationService,
                              final CasConfigurationProperties casProperties) {
        super(casProperties.getMonitor().getEndpoints().getStatistics(), casProperties);
        this.auditTrailManager = auditTrailManager;
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Gets availability times of the server.
     *
     * @return the availability
     */
    @ReadOperation
    public Map<String, Object> handle() {
        final Map<String, Object> model = new HashMap<>();

        final var diff = Duration.between(this.upTimeStartDate, ZonedDateTime.now(ZoneOffset.UTC));
        model.put("upTime", diff.getSeconds());

        final var runtime = Runtime.getRuntime();
        model.put("totalMemory", FileUtils.byteCountToDisplaySize(runtime.totalMemory()));
        model.put("maxMemory", FileUtils.byteCountToDisplaySize(runtime.maxMemory()));
        model.put("freeMemory", FileUtils.byteCountToDisplaySize(runtime.freeMemory()));

        var unexpiredTgts = 0;
        var unexpiredSts = 0;
        var expiredTgts = 0;
        var expiredSts = 0;

        final var tickets = this.centralAuthenticationService.getTickets(ticket -> true);

        for (final var ticket : tickets) {
            if (ticket instanceof ServiceTicket) {
                if (ticket.isExpired()) {
                    this.centralAuthenticationService.deleteTicket(ticket.getId());
                    expiredSts++;
                } else {
                    unexpiredSts++;
                }
            } else {
                if (ticket.isExpired()) {
                    this.centralAuthenticationService.deleteTicket(ticket.getId());
                    expiredTgts++;
                } else {
                    unexpiredTgts++;
                }
            }
        }

        model.put("unexpiredTgts", unexpiredTgts);
        model.put("unexpiredSts", unexpiredSts);
        model.put("expiredTgts", expiredTgts);
        model.put("expiredSts", expiredSts);

        return model;
    }


    /**
     * Gets authn audit summary.
     *
     * @param start the start
     * @param range the range
     * @param scale the scale
     * @return the authn audit
     */
    @ReadOperation
    public Collection<AuthenticationAuditSummary> getAuthnAuditSummary(final long start,
                                                                       final String range,
                                                                       final String scale) {

        final var audits = getAuthnAudit();
        final var startDate = DateTimeUtils.localDateTimeOf(start);
        final var endDate = startDate.plus(Duration.parse(range));

        final var authnEvents = audits.stream()
            .filter(a -> {
                final var actionTime = DateTimeUtils.localDateTimeOf(a.getWhenActionWasPerformed());
                return (actionTime.isEqual(startDate) || actionTime.isAfter(startDate))
                    && (actionTime.isEqual(endDate) || actionTime.isBefore(endDate))
                    && a.getActionPerformed().matches("AUTHENTICATION_(SUCCESS|FAILED)");
            })
            .sorted(Comparator.comparing(AuditActionContext::getWhenActionWasPerformed))
            .collect(Collectors.toList());

        final var steps = Duration.parse(scale);
        final Map<Integer, LocalDateTime> buckets = new LinkedHashMap<>();

        var dt = startDate;
        Integer index = 0;
        while (dt != null) {
            buckets.put(index++, dt);
            dt = dt.plus(steps);
            if (dt.isAfter(endDate)) {
                dt = null;
            }
        }

        final Map<LocalDateTime, AuthenticationAuditSummary> summary = new LinkedHashMap<>();
        var foundBucket = false;
        for (final var event : authnEvents) {
            foundBucket = false;
            for (var i = 0; i < buckets.keySet().size(); i++) {
                final var actionTime = DateTimeUtils.localDateTimeOf(event.getWhenActionWasPerformed());
                final var bucketDateTime = buckets.get(i);
                if (actionTime.isEqual(bucketDateTime) || actionTime.isAfter(bucketDateTime)) {
                    for (var j = 0; j < buckets.keySet().size(); j++) {
                        final var nextBucketDateTime = buckets.get(j);
                        if (actionTime.isBefore(nextBucketDateTime)) {
                            final var bucketToUse = buckets.get(j - 1);
                            final AuthenticationAuditSummary values;
                            if (summary.containsKey(bucketToUse)) {
                                values = summary.get(bucketToUse);
                            } else {
                                final var l = bucketToUse.toInstant(ZoneOffset.UTC).toEpochMilli();
                                values = new AuthenticationAuditSummary(l);
                            }
                            if (event.getActionPerformed().contains("SUCCESS")) {
                                values.incrementSuccess();
                            } else {
                                values.incrementFailure();
                            }
                            summary.put(bucketToUse, values);

                            foundBucket = true;
                            break;
                        }
                    }
                    if (foundBucket) {
                        break;
                    }
                }
            }
        }
        final var values = summary.values();
        return values;
    }

    /**
     * Gets authn audit.
     *
     * @return the authn audit
     */
    private Set<AuditActionContext> getAuthnAudit() {
        final var sinceDate = LocalDate.now().minusDays(getCasProperties().getAudit().getNumberOfDaysInHistory());
        return this.auditTrailManager.getAuditRecordsSince(sinceDate);
    }

    private static class AuthenticationAuditSummary {
        private final long time;
        private long successes;
        private long failures;

        /**
         * Instantiates a new Authentication audit summary.
         *
         * @param time the time
         */
        AuthenticationAuditSummary(final long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public long getSuccesses() {
            return successes;
        }

        public long getFailures() {
            return failures;
        }

        public void incrementSuccess() {
            successes++;
        }

        public void incrementFailure() {
            failures++;
        }
    }
}
