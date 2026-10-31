package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasConsentAdministrationTools}, available when attribute consent is included in the build and the
 * {@code mcp-write} feature is turned on.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasConsentAdministrationTools {
    private final ObjectProvider<ConsentRepository> consentRepository;

    /**
     * Revoke the attribute consent decisions of a person.
     *
     * @param username   the username
     * @param decisionId the id of a single decision, optional
     * @return the number of revoked decisions
     */
    @McpTool(description = "Revoke the attribute consent decisions of a person, or a single decision given its id from "
        + "findConsentDecisions, so CAS asks the person for consent again at the next login to those applications.",
        title = "Revoke Consent Decisions",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_OPERATOR')")
    public RevokedConsentDecisions revokeConsentDecisions(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username,
        @McpToolParam(description = "Only revoke the decision with this id, as returned by findConsentDecisions", required = false)
        @Nullable final Long decisionId) {
        val repository = consentRepository.getIfAvailable();
        if (repository == null) {
            return new RevokedConsentDecisions(username, 0, "Attribute consent is not configured");
        }
        if (decisionId != null) {
            val id = decisionId.longValue();
            val deleted = FunctionUtils.doUnchecked(() -> repository.deleteConsentDecision(id, username));
            return new RevokedConsentDecisions(username, deleted ? 1 : 0,
                deleted ? null : "No consent decision of the person has this id");
        }
        val decisions = repository.findConsentDecisions(username).size();
        FunctionUtils.doUnchecked(() -> repository.deleteConsentDecisions(username));
        return new RevokedConsentDecisions(username, decisions, null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RevokedConsentDecisions(String username, int revoked, @Nullable String reason) {
    }
}
