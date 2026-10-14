package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
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
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findAuditRecords(
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
        val result = new LinkedHashMap<String, Object>();
        result.put("username", username);
        result.put("since", since.toString());
        result.put("count", records.size());
        result.put("records", records);
        return result;
    }

    private static Map<String, Object> summarize(final AuditActionContext record) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("when", String.valueOf(record.getWhenActionWasPerformed()));
        summary.put("action", record.getActionPerformed());
        summary.put("resource", record.getResourceOperatedUpon());
        summary.put("application", record.getApplicationCode());
        val clientInfo = record.getClientInfo();
        if (clientInfo != null) {
            summary.put("clientIpAddress", clientInfo.getClientIpAddress());
            summary.put("userAgent", clientInfo.getUserAgent());
            summary.put("geoLocation", clientInfo.getGeoLocation());
        }
        return summary;
    }
}
