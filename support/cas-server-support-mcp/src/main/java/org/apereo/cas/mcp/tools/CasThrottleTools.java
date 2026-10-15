package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionHandlerInterceptor;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * This is {@link CasThrottleTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasThrottleTools {
    private static final int MAX_RECORDS = 100;

    private final CasConfigurationProperties casProperties;

    private final ObjectProvider<ThrottledSubmissionsStore> throttledSubmissionsStore;

    private final ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;

    /**
     * Find failed login attempts that CAS is tracking or throttling.
     *
     * @param username        the username, optional
     * @param clientIpAddress the client IP address, optional
     * @return the throttle settings and the matching records
     */
    @McpTool(description = "Find failed login attempts that CAS authentication throttling is tracking, and whether each one is "
        + "throttled (locked out) right now: username, client IP address, time of the last failure and the end of the lock. "
        + "Also reports the failure threshold, range and lock window. Covers in-memory and Hazelcast throttling; "
        + "throttling backed by JDBC, MongoDB or Redis keeps its records in the audit trail, so use findAuditRecords "
        + "for those. At most " + MAX_RECORDS + " records are returned, most recent first.",
        title = "Find Throttled Logins",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public ThrottledLogins findThrottledLogins(
        @McpToolParam(description = "Only records for this username, ignoring case", required = false)
        @Nullable final String username,
        @McpToolParam(description = "Only records for this client IP address", required = false)
        @Nullable final String clientIpAddress) {
        val failure = casProperties.getAuthn().getThrottle().getFailure();
        val settings = new ThrottleSettings(failure.getThreshold(), failure.getRangeSeconds(),
            failure.getThrottleWindowSeconds(), failure.getCode());
        val plan = authenticationThrottlingExecutionPlan.getIfAvailable();
        val interceptors = plan != null ? plan.getAuthenticationThrottleInterceptors() : List.<HandlerInterceptor>of();
        val interceptorTypes = interceptors.stream().map(CasDeploymentTools::typeOf).toList();
        val auditBased = interceptors.stream().anyMatch(interceptor -> !(interceptor instanceof InMemoryThrottledSubmissionHandlerInterceptor));
        val store = throttledSubmissionsStore.getIfAvailable();
        if (interceptors.isEmpty() || store == null) {
            return new ThrottledLogins(settings, interceptorTypes, auditBased, 0, List.of(),
                interceptors.isEmpty() ? "Authentication throttling is not turned on" : "No throttle store is available");
        }
        val thresholdRate = failure.getThreshold() > 0 && failure.getRangeSeconds() > 0
            ? (double) failure.getThreshold() / failure.getRangeSeconds()
            : -1D;
        val matches = ((Stream<?>) store.entries())
            .map(ThrottledSubmission.class::cast)
            .filter(submission -> StringUtils.isBlank(username) || Strings.CI.equals(submission.getUsername(), username))
            .filter(submission -> StringUtils.isBlank(clientIpAddress) || Strings.CS.equals(submission.getClientIpAddress(), clientIpAddress))
            .sorted(Comparator.comparing(ThrottledSubmission::getValue, Comparator.nullsLast(Comparator.<ZonedDateTime>reverseOrder())))
            .toList();
        val logins = matches.stream()
            .limit(MAX_RECORDS)
            .map(submission -> new ThrottledLogin(submission.getUsername(), submission.getClientIpAddress(),
                format(submission.getValue()), format(submission.getExpiration()),
                submission.isStillInExpirationWindow() || (thresholdRate > 0 && store.exceedsThreshold(submission.getKey(), thresholdRate))))
            .toList();
        return new ThrottledLogins(settings, interceptorTypes, auditBased, matches.size(), logins, null);
    }

    private static @Nullable String format(@Nullable final ZonedDateTime dateTime) {
        return dateTime != null ? DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime) : null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ThrottledLogins(ThrottleSettings settings, List<String> interceptors, boolean auditBased, long count,
                                  List<ThrottledLogin> logins, @Nullable String reason) {
    }

    public record ThrottleSettings(int failureThreshold, int rangeSeconds, String throttleWindow, String failureCode) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ThrottledLogin(@Nullable String username, @Nullable String clientIpAddress, @Nullable String lastFailure,
                                 @Nullable String lockedUntil, boolean throttled) {
    }
}
