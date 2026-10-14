package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
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
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> evaluateAuthenticationRisk(
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
        val result = new LinkedHashMap<String, Object>();
        val evaluator = authenticationRiskEvaluator.getIfAvailable();
        if (evaluator == null) {
            result.put("reason", "Risk-based authentication is not configured");
            return result;
        }
        val context = evaluationContextFactory.build(username, service);
        val registeredService = context.registeredService();
        val location = StringUtils.isNotBlank(geoLocation) ? geoLocation : null;
        val clientInfo = new ClientInfo()
            .setClientIpAddress(clientIpAddress)
            .setUserAgent(userAgent)
            .setGeoLocation(location);
        result.put("service", context.service().getId());
        result.put("principal", context.principal().getId());
        result.put("registeredService", registeredService != null ? registeredService.getName() : null);
        result.put("clientIpAddress", clientIpAddress);
        result.put("userAgent", userAgent);
        result.put("geoLocation", location);

        val calculators = evaluator.getCalculators()
            .stream()
            .map(calculator -> {
                val score = calculator.calculate(context.authentication(), registeredService, clientInfo);
                val summary = new LinkedHashMap<String, Object>();
                summary.put("calculator", calculatorName(calculator));
                summary.put("score", score != null ? score.getScore() : null);
                return summary;
            })
            .toList();
        result.put("calculators", calculators);
        val score = evaluator.evaluate(context.authentication(), registeredService, clientInfo);
        result.put("score", score.getScore());
        result.put("threshold", casProperties.getAuthn().getAdaptive().getRisk().getCore().getThreshold());
        result.put("risky", evaluator.isRiskyAuthenticationScore(score, context.authentication(), registeredService));
        return result;
    }

    private static String calculatorName(final AuthenticationRequestRiskCalculator calculator) {
        return AopUtils.getTargetClass(calculator).getSimpleName();
    }
}
