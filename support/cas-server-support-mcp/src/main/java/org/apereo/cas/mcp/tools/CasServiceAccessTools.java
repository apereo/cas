package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServiceUsernameProviderContext;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasServiceAccessTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasServiceAccessTools {
    private final CasMcpEvaluationContextFactory evaluationContextFactory;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final ConfigurableApplicationContext applicationContext;

    /**
     * Evaluate whether a person may access an application.
     *
     * @param username the username
     * @param service  the application URL
     * @return the access decision
     */
    @McpTool(description = "Evaluate whether a person is allowed to access an application, applying the access strategy "
        + "of the matching registered service to the person's resolved attributes, and explain why access is denied",
        title = "Evaluate Application Access",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> evaluateAccess(
        @McpToolParam(description = "The username of the person")
        final String username,
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login")
        final String service) {
        return evaluate(username, service).describe();
    }

    /**
     * Preview the username and attributes CAS would release to an application for a person.
     *
     * @param username the username
     * @param service  the application URL
     * @return the released username and attributes
     */
    @McpTool(description = "Preview the username and attributes CAS would release to an application for a person, "
        + "applying the attribute release policy and username provider of the matching registered service",
        title = "Preview Attribute Release",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> previewAttributeRelease(
        @McpToolParam(description = "The username of the person")
        final String username,
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login")
        final String service) {
        val access = evaluate(username, service);
        val result = access.describe();
        val registeredService = access.registeredService();
        if (registeredService != null) {
            val releaseContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .service(access.service())
                .principal(access.principal())
                .applicationContext(applicationContext)
                .build();
            val attributes = FunctionUtils.doUnchecked(() -> registeredService.getAttributeReleasePolicy().getAttributes(releaseContext));
            val usernameContext = RegisteredServiceUsernameProviderContext.builder()
                .registeredService(registeredService)
                .service(access.service())
                .principal(access.principal())
                .releasingAttributes(attributes)
                .applicationContext(applicationContext)
                .build();
            result.put("releasedUsername", FunctionUtils.doUnchecked(
                () -> registeredService.getUsernameAttributeProvider().resolveUsername(usernameContext)));
            result.put("releasedAttributes", new TreeMap<>(attributes));
        }
        return result;
    }

    private AccessEvaluation evaluate(final String username, final String serviceUrl) {
        val evaluationContext = evaluationContextFactory.build(username, serviceUrl);
        val service = evaluationContext.service();
        val registeredService = evaluationContext.registeredService();
        val principal = evaluationContext.principal();
        if (registeredService == null) {
            return new AccessEvaluation(service, null, principal, "No registered service matches " + service.getId());
        }
        val context = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .principal(principal)
            .build();
        val denialReason = FunctionUtils.doUnchecked(() -> registeredServiceAccessStrategyEnforcer.execute(context))
            .getException()
            .map(exception -> Objects.requireNonNullElseGet(exception.getMessage(), () -> exception.getClass().getSimpleName()))
            .orElse(null);
        return new AccessEvaluation(service, registeredService, principal, denialReason);
    }

    private record AccessEvaluation(WebApplicationService service, @Nullable RegisteredService registeredService,
                                    Principal principal, @Nullable String denialReason) {
        Map<String, Object> describe() {
            val result = new LinkedHashMap<String, Object>();
            result.put("service", service.getId());
            result.put("principal", principal.getId());
            if (registeredService != null) {
                val summary = new LinkedHashMap<String, Object>();
                summary.put("id", registeredService.getId());
                summary.put("name", registeredService.getName());
                summary.put("serviceId", registeredService.getServiceId());
                summary.put("type", registeredService.getFriendlyName());
                result.put("registeredService", summary);
            }
            result.put("accessAllowed", denialReason == null);
            if (denialReason != null) {
                result.put("reason", denialReason);
            }
            return result;
        }
    }
}
