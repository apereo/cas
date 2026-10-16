package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasAuthenticationRiskTools}, available when risk-based authentication is included in the build.
 * The client details of the attempt are supplied by the caller, since those of the MCP request describe the operator.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasAuthenticationRiskTools {
    private final CasMcpEvaluationContextFactory evaluationContextFactory;

    private final ObjectProvider<AuthenticationRiskEvaluator> authenticationRiskEvaluator;

    private final CasConfigurationProperties casProperties;

    /**
     * Evaluate how risky a login attempt by a person from a given client would be.
     *
     * @param username        the username
     * @param service         the application URL
     * @param clientIpAddress the client IP address of the attempt
     * @param userAgent       the user agent of the attempt
     * @param geoLocation     the optional location of the attempt, as latitude,longitude
     * @return the score of each risk calculator, the overall score and whether the attempt counts as risky
     */
    @McpTool(description = "Evaluate how risky a login attempt by a person to an application would be when made from the "
        + "given client IP address, user agent and optional location, comparing it with the person's recorded sign-ins. "
        + "Reports the score of each risk calculator, the overall score, the configured threshold and whether CAS would "
        + "treat the attempt as risky. Each evaluation is recorded in the audit log.",
        title = "Evaluate Authentication Risk",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public RiskEvaluation evaluateAuthenticationRisk(
        @McpToolParam(description = "The username of the person")
        final String username,
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login")
        final String service,
        @McpToolParam(description = "The client IP address the login attempt comes from")
        final String clientIpAddress,
        @McpToolParam(description = "The user agent (browser) of the login attempt")
        final String userAgent,
        @McpToolParam(description = "The location of the login attempt as latitude,longitude", required = false)
        final @Nullable String geoLocation) {
        val client = CasMcpEvaluationContextFactory.CasMcpClient.of(clientIpAddress, userAgent, geoLocation);
        val evaluator = authenticationRiskEvaluator.getIfAvailable();
        if (evaluator == null) {
            return new RiskEvaluation(service, null, null, client.describe(), List.of(), null, null, null,
                "Risk-based authentication is not configured");
        }
        val context = evaluationContextFactory.build(username, service);
        val registeredService = context.registeredService();
        val clientInfo = evaluationContextFactory.newClientInfo(client);
        val calculators = evaluator.getCalculators()
            .stream()
            .map(calculator -> {
                val score = calculator.calculate(context.authentication(), registeredService, clientInfo);
                return new CalculatorScore(calculatorName(calculator), score != null ? score.getScore() : null);
            })
            .toList();
        val score = evaluator.evaluate(context.authentication(), registeredService, clientInfo);
        return new RiskEvaluation(context.service().getId(), context.principal().getId(),
            registeredService != null ? registeredService.getName() : null, client.describe(), calculators, score.getScore(),
            casProperties.getAuthn().getAdaptive().getRisk().getCore().getThreshold(),
            evaluator.isRiskyAuthenticationScore(score, context.authentication(), registeredService), null);
    }

    private static String calculatorName(final AuthenticationRequestRiskCalculator calculator) {
        return AopUtils.getTargetClass(calculator).getSimpleName();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RiskEvaluation(String service, @Nullable String principal, @Nullable String registeredService,
                                 Map<String, Object> client, List<CalculatorScore> calculators, @Nullable BigDecimal score,
                                 @Nullable Double threshold, @Nullable Boolean risky, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CalculatorScore(String calculator, @Nullable BigDecimal score) {
    }
}
