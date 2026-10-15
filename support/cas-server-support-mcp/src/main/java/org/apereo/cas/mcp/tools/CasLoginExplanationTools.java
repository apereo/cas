package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * This is {@link CasLoginExplanationTools}. It runs the other evaluation tools in the order CAS applies them during
 * login and condenses their results into one walk-through; the nested calls are not audited separately.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasLoginExplanationTools {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final CasConfigurationProperties casProperties;

    private final ObjectProvider<CasServiceAccessTools> serviceAccessTools;

    private final ObjectProvider<CasMultifactorAuthenticationTools> multifactorAuthenticationTools;

    private final ObjectProvider<CasAuthenticationRiskTools> authenticationRiskTools;

    private final ObjectProvider<CasInterruptTools> interruptTools;

    /**
     * Explain what would happen when a person logs into an application.
     *
     * @param username        the username
     * @param service         the application URL
     * @param clientIpAddress the optional client IP address of the login attempt
     * @param userAgent       the optional user agent of the login attempt
     * @param geoLocation     the optional location of the login attempt, as latitude,longitude
     * @return the outcome and the steps that led to it
     */
    @McpTool(description = "Explain what would happen if a person logged into an application, in one call: whether a "
        + "registered service matches and its access strategy lets the person in, how risky the attempt is (when a client "
        + "IP address and user agent are given and risk-based authentication is installed), which multifactor provider "
        + "would be required, whether an interrupt would be shown or block the login, and which username and attributes "
        + "would be released. Stops at the first step that denies the login. Each step reports its status and the result "
        + "of the tool that evaluates it (evaluateAccess, evaluateAuthenticationRisk, evaluateMultifactorTriggers, "
        + "previewInterrupt, previewAttributeRelease); call those for more detail. Passwords are not checked.",
        title = "Explain Login",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = true))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public LoginExplanation explainLogin(
        @McpToolParam(description = "The username of the person")
        final String username,
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login")
        final String service,
        @McpToolParam(description = "The client IP address the login attempt comes from", required = false)
        @Nullable final String clientIpAddress,
        @McpToolParam(description = "The user agent (browser) of the login attempt", required = false)
        @Nullable final String userAgent,
        @McpToolParam(description = "The location of the login attempt as latitude,longitude", required = false)
        @Nullable final String geoLocation) {
        val steps = new ArrayList<LoginStep>();
        val accessTools = serviceAccessTools.getObject();

        val access = asMap(accessTools.evaluateAccess(username, service));
        if (!access.containsKey("registeredService")) {
            steps.add(new LoginStep("access", StepStatus.BLOCKED, "No registered service matches the application URL", access));
            return new LoginExplanation(username, service, Outcome.SERVICE_NOT_REGISTERED, null, false, null, steps);
        }
        if (!Boolean.TRUE.equals(access.get("accessAllowed"))) {
            steps.add(new LoginStep("access", StepStatus.BLOCKED,
                StringUtils.defaultIfBlank((String) access.get("reason"), "The access strategy denies access"), access));
            return new LoginExplanation(username, service, Outcome.ACCESS_DENIED, null, false, null, steps);
        }
        steps.add(new LoginStep("access", StepStatus.PASSED, "The access strategy of the registered service allows access", access));

        val risky = evaluateRisk(username, service, clientIpAddress, userAgent, geoLocation, steps);
        if (Boolean.TRUE.equals(risky) && casProperties.getAuthn().getAdaptive().getRisk().getResponse().isBlockAttempt()) {
            return new LoginExplanation(username, service, Outcome.BLOCKED_AS_RISKY, null, false, true, steps);
        }

        val multifactor = asMap(multifactorAuthenticationTools.getObject()
            .evaluateMultifactorTriggers(username, service, clientIpAddress, userAgent, geoLocation));
        val provider = multifactor.get("provider") instanceof final Map<?, ?> selected ? String.valueOf(selected.get("id")) : null;
        if (provider != null) {
            steps.add(new LoginStep("multifactor", StepStatus.TRIGGERED, "Multifactor authentication with " + provider + " is required", multifactor));
        } else if (multifactor.get("triggers") instanceof final List<?> triggers
            && triggers.stream().anyMatch(trigger -> trigger instanceof final Map<?, ?> evaluated && evaluated.containsKey("error"))) {
            steps.add(new LoginStep("multifactor", StepStatus.FAILED, "A multifactor trigger failed; see its error", multifactor));
        } else {
            steps.add(new LoginStep("multifactor", StepStatus.PASSED, "No multifactor authentication is required", multifactor));
        }

        val interrupted = evaluateInterrupt(username, service, steps);
        if (interrupted == StepStatus.BLOCKED) {
            return new LoginExplanation(username, service, Outcome.BLOCKED_BY_INTERRUPT, provider, true, risky, steps);
        }

        val release = asMap(accessTools.previewAttributeRelease(username, service));
        val attributes = release.get("releasedAttributes") instanceof final Map<?, ?> released ? released.size() : 0;
        steps.add(new LoginStep("attributeRelease", StepStatus.PASSED,
            "Username %s and %s attribute(s) would be released".formatted(release.get("releasedUsername"), attributes), release));
        return new LoginExplanation(username, service, Outcome.LOGIN_SUCCEEDS, provider, interrupted == StepStatus.TRIGGERED, risky, steps);
    }

    private @Nullable Boolean evaluateRisk(final String username, final String service, @Nullable final String clientIpAddress,
                                           @Nullable final String userAgent, @Nullable final String geoLocation,
                                           final List<LoginStep> steps) {
        val riskTools = authenticationRiskTools.getIfAvailable();
        if (riskTools == null) {
            steps.add(new LoginStep("risk", StepStatus.SKIPPED, "Risk-based authentication is not available", Map.of()));
            return null;
        }
        if (StringUtils.isAnyBlank(clientIpAddress, userAgent)) {
            steps.add(new LoginStep("risk", StepStatus.SKIPPED, "No client IP address and user agent were given", Map.of()));
            return null;
        }
        val risk = asMap(riskTools.evaluateAuthenticationRisk(username, service,
            Objects.requireNonNull(clientIpAddress), Objects.requireNonNull(userAgent), geoLocation));
        if (risk.containsKey("reason")) {
            steps.add(new LoginStep("risk", StepStatus.SKIPPED, String.valueOf(risk.get("reason")), risk));
            return null;
        }
        val risky = Boolean.TRUE.equals(risk.get("risky"));
        val blocks = casProperties.getAuthn().getAdaptive().getRisk().getResponse().isBlockAttempt();
        if (risky) {
            steps.add(new LoginStep("risk", blocks ? StepStatus.BLOCKED : StepStatus.TRIGGERED,
                blocks ? "The attempt is risky and risky attempts are blocked" : "The attempt is risky", risk));
        } else {
            steps.add(new LoginStep("risk", StepStatus.PASSED, "The attempt is not risky", risk));
        }
        return risky;
    }

    private StepStatus evaluateInterrupt(final String username, final String service, final List<LoginStep> steps) {
        val tools = interruptTools.getIfAvailable();
        if (tools == null) {
            steps.add(new LoginStep("interrupt", StepStatus.SKIPPED, "Authentication interrupts are not available", Map.of()));
            return StepStatus.SKIPPED;
        }
        val preview = asMap(tools.previewInterrupt(username, service));
        if (preview.containsKey("reason")) {
            steps.add(new LoginStep("interrupt", StepStatus.SKIPPED, String.valueOf(preview.get("reason")), preview));
            return StepStatus.SKIPPED;
        }
        if (preview.get("interrupt") instanceof final Map<?, ?> interrupt) {
            val blocks = Boolean.TRUE.equals(interrupt.get("block"));
            val status = blocks ? StepStatus.BLOCKED : StepStatus.TRIGGERED;
            steps.add(new LoginStep("interrupt", status, blocks
                ? "An interrupt is shown and blocks the login: " + interrupt.get("message")
                : "An interrupt is shown: " + interrupt.get("message"), preview));
            return status;
        }
        steps.add(new LoginStep("interrupt", StepStatus.PASSED, "No interrupt is shown", preview));
        return StepStatus.PASSED;
    }

    private static Map<String, Object> asMap(final Object result) {
        return MAPPER.convertValue(result, MAP_TYPE);
    }

    /**
     * The overall outcome of the login.
     */
    public enum Outcome {
        /**
         * No registered service matches the application URL.
         */
        SERVICE_NOT_REGISTERED,
        /**
         * The access strategy of the registered service denies the person.
         */
        ACCESS_DENIED,
        /**
         * The attempt is risky and risky attempts are blocked.
         */
        BLOCKED_AS_RISKY,
        /**
         * An interrupt blocks the login.
         */
        BLOCKED_BY_INTERRUPT,
        /**
         * The login succeeds, possibly after multifactor authentication or an interrupt.
         */
        LOGIN_SUCCEEDS
    }

    /**
     * The status of one login step.
     */
    public enum StepStatus {
        /**
         * The step lets the login continue.
         */
        PASSED,
        /**
         * The step adds a requirement, such as multifactor authentication or an interrupt, but lets the login continue.
         */
        TRIGGERED,
        /**
         * The step stops the login.
         */
        BLOCKED,
        /**
         * The step was not evaluated.
         */
        SKIPPED,
        /**
         * The step could not be evaluated.
         */
        FAILED
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LoginExplanation(String username, String service, Outcome outcome, @Nullable String multifactorProvider,
                                   boolean interrupted, @Nullable Boolean risky, List<LoginStep> steps) {
    }

    public record LoginStep(String step, StepStatus status, String summary, Map<String, Object> result) {
    }
}
