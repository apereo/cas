package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link CasMultifactorAuthenticationTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasMultifactorAuthenticationTools {
    private final CasMcpEvaluationContextFactory evaluationContextFactory;

    private final ObjectProvider<MultifactorAuthenticationTriggerSelectionStrategy> triggerSelectionStrategy;

    private final ConfigurableApplicationContext applicationContext;

    /**
     * List the configured multifactor authentication providers.
     *
     * @return the providers
     */
    @McpTool(description = "List the multifactor authentication providers configured in CAS, "
        + "with their ids, names, order and failure modes",
        title = "List Multifactor Providers",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> listMultifactorProviders() {
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(Comparator.comparingInt(MultifactorAuthenticationProvider::getOrder))
            .map(CasMultifactorAuthenticationTools::summarize)
            .toList();
        val result = new LinkedHashMap<String, Object>();
        result.put("count", providers.size());
        result.put("providers", providers);
        return result;
    }

    /**
     * Evaluate which multifactor authentication provider CAS would require for a person and an application.
     *
     * @param username the username
     * @param service  the application URL
     * @return the triggers that were evaluated and the provider they selected
     */
    @McpTool(description = "Evaluate which multifactor authentication provider, if any, CAS would require for a person "
        + "logging into an application, running the multifactor authentication triggers in order and reporting which one "
        + "selected the provider. Triggers that depend on the HTTP request see a request without parameters or headers.",
        title = "Evaluate Multifactor Triggers",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> evaluateMultifactorTriggers(
        @McpToolParam(description = "The username of the person")
        final String username,
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login")
        final String service) {
        val strategy = triggerSelectionStrategy.getIfAvailable();
        if (strategy == null) {
            throw new IllegalStateException("Multifactor authentication triggers are not available");
        }
        val exchange = evaluationContextFactory.currentHttpExchange();
        val context = evaluationContextFactory.build(username, service);
        val webApplicationService = context.service();
        val registeredService = context.registeredService();
        val principal = context.principal();
        val authentication = context.authentication();
        val request = exchange.request();
        val response = exchange.response();

        val result = new LinkedHashMap<String, Object>();
        result.put("service", webApplicationService.getId());
        result.put("principal", principal.getId());
        result.put("registeredService", registeredService != null ? registeredService.getName() : null);

        val evaluated = new ArrayList<Map<String, Object>>();
        result.put("triggers", evaluated);
        if (registeredService != null && registeredService.getMultifactorAuthenticationPolicy().isBypassEnabled()) {
            result.put("bypassedByService", true);
            result.put("provider", null);
            return result;
        }
        val triggers = strategy.getMultifactorAuthenticationTriggers()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(Comparator.comparingInt(Ordered::getOrder))
            .toList();
        for (val trigger : triggers) {
            val outcome = new LinkedHashMap<String, Object>();
            outcome.put("trigger", trigger.getName());
            evaluated.add(outcome);
            if (!trigger.supports(request, registeredService, authentication, webApplicationService)) {
                outcome.put("outcome", "NOT_SUPPORTED");
                continue;
            }
            val activated = evaluate(trigger, authentication, registeredService, request, response, webApplicationService, outcome);
            if (activated != null) {
                outcome.put("outcome", "ACTIVATED");
                result.put("provider", summarize(activated));
                return result;
            }
            if (outcome.containsKey("error")) {
                return result;
            }
            outcome.put("outcome", "NOT_ACTIVATED");
        }
        result.put("provider", null);
        return result;
    }

    private static @Nullable MultifactorAuthenticationProvider evaluate(
        final MultifactorAuthenticationTrigger trigger,
        final Authentication authentication,
        final @Nullable RegisteredService registeredService,
        final HttpServletRequest request, final HttpServletResponse response,
        final WebApplicationService service, final Map<String, Object> outcome) {
        try {
            return trigger.isActivated(authentication, registeredService, request, response, service).orElse(null);
        } catch (final Throwable e) {
            outcome.put("outcome", "FAILED");
            outcome.put("error", Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()));
            return null;
        }
    }

    private static Map<String, Object> summarize(final MultifactorAuthenticationProvider provider) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("id", provider.getId());
        summary.put("name", provider.getFriendlyName());
        summary.put("order", provider.getOrder());
        summary.put("failureMode", String.valueOf(provider.getFailureMode()));
        return summary;
    }
}
