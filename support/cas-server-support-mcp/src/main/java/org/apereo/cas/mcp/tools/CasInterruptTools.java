package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.services.WebBasedRegisteredService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CasInterruptTools}, available when authentication interrupts are included in the build.
 * Interrupt sources run in order and the first one that interrupts wins; a failing source is recorded and skipped,
 * as during login. Sources may call external endpoints or run scripts.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasInterruptTools {
    private final CasMcpEvaluationContextFactory evaluationContextFactory;

    private final ObjectProvider<InterruptInquiryExecutionPlan> interruptInquiryExecutionPlan;

    /**
     * Preview the interrupt CAS would show a person after authenticating to an application.
     *
     * @param username the username
     * @param service  the application URL
     * @return the interrupt sources that were asked and the interrupt they produced, if any
     */
    @McpTool(description = "Preview the interrupt notification CAS would show a person right after authentication when "
        + "logging into an application, asking each interrupt source in order and reporting which one interrupts, with "
        + "its message, links and whether it blocks the login. The interrupt policy of the matching registered service is "
        + "reported, but its attribute and script conditions are not evaluated. Interrupt sources may call external "
        + "endpoints or run scripts.",
        title = "Preview Interrupt",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = true))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> previewInterrupt(
        @McpToolParam(description = "The username of the person")
        final String username,
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login")
        final String service) {
        val result = new LinkedHashMap<String, Object>();
        val plan = interruptInquiryExecutionPlan.getIfAvailable();
        if (plan == null) {
            result.put("reason", "Authentication interrupts are not configured");
            return result;
        }
        val context = evaluationContextFactory.build(username, service);
        val requestContext = evaluationContextFactory.newWebflowRequestContext(context);
        val registeredService = context.registeredService();
        result.put("service", context.service().getId());
        result.put("principal", context.principal().getId());
        result.put("registeredService", registeredService != null ? registeredService.getName() : null);
        if (registeredService instanceof final WebBasedRegisteredService webBasedService
            && webBasedService.getWebflowInterruptPolicy() != null) {
            val policy = webBasedService.getWebflowInterruptPolicy();
            val summary = new LinkedHashMap<String, Object>();
            summary.put("enabled", policy.isEnabled());
            summary.put("forceExecution", String.valueOf(policy.getForceExecution()));
            summary.put("attributeName", policy.getAttributeName());
            summary.put("attributeValue", policy.getAttributeValue());
            summary.put("groovyScriptDefined", StringUtils.isNotBlank(policy.getGroovyScript()));
            result.put("interruptPolicy", summary);
        }
        val inquirers = new ArrayList<Map<String, Object>>();
        result.put("inquirers", inquirers);
        result.put("interrupt", null);
        for (val inquirer : plan.getInterruptInquirers()) {
            val outcome = new LinkedHashMap<String, Object>();
            outcome.put("inquirer", inquirer.getName());
            inquirers.add(outcome);
            val response = inquire(inquirer, context, requestContext, outcome);
            if (response != null && response.isInterrupt()) {
                outcome.put("outcome", "INTERRUPT");
                result.put("interrupt", summarize(response));
                return result;
            }
            outcome.putIfAbsent("outcome", "NO_INTERRUPT");
        }
        return result;
    }

    private static @Nullable InterruptResponse inquire(final InterruptInquirer inquirer,
                                                       final CasMcpEvaluationContextFactory.CasMcpEvaluationContext context,
                                                       final RequestContext requestContext,
                                                       final Map<String, Object> outcome) {
        try {
            return inquirer.inquire(context.authentication(), context.registeredService(),
                context.service(), context.credential(), requestContext);
        } catch (final Throwable e) {
            outcome.put("outcome", "FAILED");
            outcome.put("error", Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()));
            return null;
        }
    }

    private static Map<String, Object> summarize(final InterruptResponse response) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("message", response.getMessage());
        summary.put("messageCode", response.getMessageCode());
        summary.put("block", response.isBlock());
        summary.put("ssoEnabled", response.isSsoEnabled());
        summary.put("autoRedirect", response.isAutoRedirect());
        summary.put("links", response.getLinks());
        return summary;
    }
}
