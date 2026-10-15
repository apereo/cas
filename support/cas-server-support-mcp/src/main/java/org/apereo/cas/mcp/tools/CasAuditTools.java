package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasAuditTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasAuditTools {
    private static final int MAX_RECORDS = 100;

    private static final int DEFAULT_DAYS = 7;

    private final AuditTrailExecutionPlan auditTrailExecutionPlan;

    /**
     * Find the most recent audit records of a person.
     *
     * @param username the username
     * @param days     how many days to look back
     * @param action   an optional action filter
     * @return the matching audit records, newest first
     */
    @McpTool(description = "Find the audit records of a person, newest first, such as authentication failures, "
        + "ticket validations and access denials. At most " + MAX_RECORDS + " records are returned.",
        title = "Find Audit Records",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public AuditRecords findAuditRecords(
        @McpToolParam(description = "The username (principal) recorded in the audit log")
        final String username,
        @McpToolParam(description = "How many days to look back; defaults to " + DEFAULT_DAYS, required = false)
        final @Nullable Integer days,
        @McpToolParam(description = "Only return records whose action contains this text, such as AUTHENTICATION_FAILED",
            required = false)
        final @Nullable String action) {
        val lookBack = days != null && days > 0 ? days : DEFAULT_DAYS;
        val since = LocalDateTime.now(Clock.systemUTC()).minusDays(lookBack);
        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(
            AuditTrailManager.WhereClauseFields.DATE, since,
            AuditTrailManager.WhereClauseFields.PRINCIPAL, username,
            AuditTrailManager.WhereClauseFields.COUNT, (long) MAX_RECORDS);
        val records = auditTrailExecutionPlan.getAuditRecords(criteria)
            .stream()
            .filter(record -> Strings.CI.equals(record.getPrincipal(), username))
            .filter(record -> StringUtils.isBlank(action) || Strings.CI.contains(record.getActionPerformed(), action))
            .filter(record -> record.getWhenActionWasPerformed() != null && record.getWhenActionWasPerformed().isAfter(since))
            .sorted(Comparator.comparing(AuditActionContext::getWhenActionWasPerformed).reversed())
            .limit(MAX_RECORDS)
            .map(CasAuditTools::summarize)
            .toList();
        return new AuditRecords(username, since.toString(), records.size(), records);
    }

    private static AuditRecord summarize(final AuditActionContext record) {
        val clientInfo = record.getClientInfo();
        return new AuditRecord(String.valueOf(record.getWhenActionWasPerformed()), record.getActionPerformed(),
            record.getResourceOperatedUpon(), record.getApplicationCode(),
            clientInfo != null ? clientInfo.getClientIpAddress() : null,
            clientInfo != null ? clientInfo.getUserAgent() : null,
            clientInfo != null ? clientInfo.getGeoLocation() : null);
    }

    public record AuditRecords(String username, String since, int count, List<AuditRecord> records) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AuditRecord(String when, @Nullable String action, @Nullable String resource, @Nullable String application,
                              @Nullable String clientIpAddress, @Nullable String userAgent, @Nullable String geoLocation) {
    }
}
