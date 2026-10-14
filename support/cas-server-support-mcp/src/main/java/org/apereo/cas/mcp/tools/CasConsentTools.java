package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.consent.ConsentDecision;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasConsentTools}, available when attribute consent support is included in the build.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasConsentTools {
    private final ObjectProvider<ConsentRepository> consentRepository;

    private final ObjectProvider<ConsentDecisionBuilder> consentDecisionBuilder;

    /**
     * Find the attribute consent decisions of a person.
     *
     * @param username the username
     * @return the consent decisions
     */
    @McpTool(description = "Find the attribute consent decisions a person has made: the application, when consent was given, "
        + "how often the person is reminded and the names of the attributes consented to",
        title = "Find Consent Decisions",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findConsentDecisions(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username) {
        val result = new LinkedHashMap<String, Object>();
        result.put("username", username);
        val repository = consentRepository.getIfAvailable();
        if (repository == null) {
            result.put("reason", "Attribute consent is not configured");
            return result;
        }
        val builder = consentDecisionBuilder.getIfAvailable();
        val decisions = repository.findConsentDecisions(username)
            .stream()
            .sorted(Comparator.comparing(ConsentDecision::getCreatedDate, Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())))
            .map(decision -> summarize(decision, builder))
            .toList();
        result.put("count", decisions.size());
        result.put("decisions", decisions);
        return result;
    }

    private static Map<String, Object> summarize(final ConsentDecision decision,
                                                 final @Nullable ConsentDecisionBuilder builder) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("id", String.valueOf(decision.getId()));
        summary.put("service", decision.getService());
        summary.put("createdDate", String.valueOf(decision.getCreatedDate()));
        summary.put("options", String.valueOf(decision.getOptions()));
        summary.put("reminder", decision.getReminder() + " " + decision.getReminderTimeUnit());
        if (builder != null) {
            summary.put("attributes", new TreeSet<>(builder.getConsentableAttributesFrom(decision).keySet()));
        }
        return summary;
    }
}
