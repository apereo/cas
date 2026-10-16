package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.services.RegisteredServiceWebflowInterruptPolicy;
import org.apereo.cas.services.WebBasedRegisteredService;
import com.fasterxml.jackson.annotation.JsonInclude;
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
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = true))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public InterruptPreview previewInterrupt(
        @McpToolParam(description = "The username of the person")
        final String username,
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login")
        final String service) {
        val plan = interruptInquiryExecutionPlan.getIfAvailable();
        if (plan == null) {
            return new InterruptPreview(service, null, null, null, List.of(), null, "Authentication interrupts are not configured");
        }
        val context = evaluationContextFactory.build(username, service);
        val requestContext = evaluationContextFactory.newWebflowRequestContext(context);
        val registeredService = context.registeredService();
        val interruptPolicy = registeredService instanceof final WebBasedRegisteredService webBasedService
            && webBasedService.getWebflowInterruptPolicy() != null
            ? summarize(webBasedService.getWebflowInterruptPolicy())
            : null;
        val inquirers = new ArrayList<InquirerOutcome>();
        for (val inquirer : plan.getInterruptInquirers()) {
            val failure = new AtomicReference<String>();
            val response = inquire(inquirer, context, requestContext, failure);
            if (response != null && response.isInterrupt()) {
                inquirers.add(new InquirerOutcome(inquirer.getName(), "INTERRUPT", null));
                return new InterruptPreview(context.service().getId(), context.principal().getId(),
                    registeredService != null ? registeredService.getName() : null, interruptPolicy, inquirers, summarize(response), null);
            }
            inquirers.add(failure.get() != null
                ? new InquirerOutcome(inquirer.getName(), "FAILED", failure.get())
                : new InquirerOutcome(inquirer.getName(), "NO_INTERRUPT", null));
        }
        return new InterruptPreview(context.service().getId(), context.principal().getId(),
            registeredService != null ? registeredService.getName() : null, interruptPolicy, inquirers, null, null);
    }

    private static @Nullable InterruptResponse inquire(final InterruptInquirer inquirer,
                                                       final CasMcpEvaluationContextFactory.CasMcpEvaluationContext context,
                                                       final RequestContext requestContext,
                                                       final AtomicReference<String> failure) {
        try {
            return inquirer.inquire(context.authentication(), context.registeredService(),
                context.service(), context.credential(), requestContext);
        } catch (final Throwable e) {
            failure.set(Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()));
            return null;
        }
    }

    private static InterruptPolicySummary summarize(final RegisteredServiceWebflowInterruptPolicy policy) {
        return new InterruptPolicySummary(policy.isEnabled(), String.valueOf(policy.getForceExecution()),
            policy.getAttributeName(), policy.getAttributeValue(), StringUtils.isNotBlank(policy.getGroovyScript()));
    }

    private static Interrupt summarize(final InterruptResponse response) {
        return new Interrupt(response.getMessage(), response.getMessageCode(), response.isBlock(), response.isSsoEnabled(),
            response.isAutoRedirect(), response.getLinks() != null ? new LinkedHashMap<>(response.getLinks()) : Map.of());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record InterruptPreview(String service, @Nullable String principal, @Nullable String registeredService,
                                   @Nullable InterruptPolicySummary interruptPolicy, List<InquirerOutcome> inquirers,
                                   @Nullable Interrupt interrupt, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record InterruptPolicySummary(boolean enabled, String forceExecution, @Nullable String attributeName,
                                         @Nullable String attributeValue, boolean groovyScriptDefined) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record InquirerOutcome(String inquirer, String outcome, @Nullable String error) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Interrupt(@Nullable String message, @Nullable String messageCode, boolean block, boolean ssoEnabled,
                            boolean autoRedirect, Map<String, String> links) {
    }
}
