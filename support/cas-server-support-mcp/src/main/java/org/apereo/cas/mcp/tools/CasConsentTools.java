package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.consent.ConsentDecision;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
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
     * @return the consent decisions, newest first
     */
    @McpTool(description = "Find the attribute consent decisions a person has made: the application, when consent was given, "
        + "how often the person is reminded and the names of the attributes consented to",
        title = "Find Consent Decisions",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public ConsentDecisions findConsentDecisions(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username) {
        val repository = consentRepository.getIfAvailable();
        if (repository == null) {
            return new ConsentDecisions(username, 0, List.of(), "Attribute consent is not configured");
        }
        val builder = consentDecisionBuilder.getIfAvailable();
        val decisions = repository.findConsentDecisions(username)
            .stream()
            .sorted(Comparator.comparing(ConsentDecision::getCreatedDate, Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())))
            .map(decision -> summarize(decision, builder))
            .toList();
        return new ConsentDecisions(username, decisions.size(), decisions, null);
    }

    private static ConsentDecisionSummary summarize(final ConsentDecision decision,
                                                    final @Nullable ConsentDecisionBuilder builder) {
        val attributes = builder != null
            ? new TreeSet<>(builder.getConsentableAttributesFrom(decision).keySet()).stream().toList()
            : null;
        return new ConsentDecisionSummary(String.valueOf(decision.getId()), decision.getService(),
            String.valueOf(decision.getCreatedDate()), String.valueOf(decision.getOptions()),
            decision.getReminder() + " " + decision.getReminderTimeUnit(), attributes);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ConsentDecisions(String username, int count, List<ConsentDecisionSummary> decisions, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ConsentDecisionSummary(String id, @Nullable String service, String createdDate, String options, String reminder,
                                         @Nullable List<String> attributes) {
    }
}
