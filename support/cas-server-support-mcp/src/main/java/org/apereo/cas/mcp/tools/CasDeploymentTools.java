package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.services.ChainingServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.feature.CasRuntimeModule;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ClassUtils;

/**
 * This is {@link CasDeploymentTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasDeploymentTools {
    private static final Map<String, String> MCP_TOOL_GROUPS = new TreeMap<>(Map.ofEntries(
        Map.entry("mcp-consent", "casConsentTools"),
        Map.entry("mcp-saml-idp", "casSamlIdPMetadataTools"),
        Map.entry("mcp-oidc", "casOidcTools"),
        Map.entry("mcp-trusted-mfa", "casTrustedDeviceTools"),
        Map.entry("mcp-surrogate", "casSurrogateTools"),
        Map.entry("mcp-interrupt", "casInterruptTools"),
        Map.entry("mcp-electrofence", "casAuthenticationRiskTools"),
        Map.entry("mcp-otp", "casOneTimeTokenTools"),
        Map.entry("mcp-webauthn", "casWebAuthnTools"),
        Map.entry("mcp-passwordless", "casPasswordlessTools"),
        Map.entry("mcp-delegated", "casDelegatedAuthenticationTools"),
        Map.entry("mcp-heimdall", "casHeimdallTools"),
        Map.entry("mcp-settings", "casSettingTools"),
        Map.entry("mcp-audit", "casMcpAuditor")));

    private static final String FEATURE_PREFIX = CasFeatureModule.class.getSimpleName() + '.';

    private final CasConfigurationProperties casProperties;

    private final ConfigurableApplicationContext applicationContext;

    private final TicketRegistry ticketRegistry;

    private final ObjectProvider<ServiceRegistry> serviceRegistry;

    private final ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    private final ObjectProvider<PersonDirectoryAttributeRepositoryPlan> attributeRepositoryPlan;

    private final ObjectProvider<CasRuntimeModuleLoader> runtimeModuleLoader;

    /**
     * Describe what this CAS deployment runs.
     *
     * @return the deployment description
     */
    @McpTool(description = "Describe this CAS deployment: CAS version, server name and prefix, active profiles, the feature "
        + "modules that are turned on, the CAS modules on the classpath, ticket and service registry types, the "
        + "authentication handlers and policies, the attribute repositories, and which optional MCP tool groups are "
        + "active (with the property that turns each one on). Call this first to learn what the other tools can see.",
        title = "Describe Deployment",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Deployment describeDeployment() {
        return new Deployment(
            StringUtils.defaultIfBlank(CasVersion.getVersion(), "unknown"),
            casProperties.getServer().getName(),
            casProperties.getServer().getPrefix(),
            List.of(applicationContext.getEnvironment().getActiveProfiles()),
            features(),
            modules(),
            typeOf(ticketRegistry),
            serviceRegistries(),
            authenticationHandlers(),
            authenticationPolicies(),
            attributeRepositories(),
            mcpToolGroups());
    }

    private static List<String> features() {
        return CasFeatureModule.FeatureCatalog.getRegisteredFeatures()
            .stream()
            .map(feature -> Strings.CS.removeStart(feature, FEATURE_PREFIX))
            .sorted()
            .toList();
    }

    private List<String> modules() {
        val loader = runtimeModuleLoader.getIfAvailable();
        if (loader == null) {
            return List.of();
        }
        return FunctionUtils.doAndHandle(() -> loader.load().stream().map(CasRuntimeModule::getName).sorted().toList(),
            e -> List.<String>of()).get();
    }

    private List<String> serviceRegistries() {
        val registry = serviceRegistry.getIfAvailable();
        if (registry == null) {
            return List.of();
        }
        if (registry instanceof final ChainingServiceRegistry chain) {
            return chain.getServiceRegistries().stream().map(CasDeploymentTools::typeOf).toList();
        }
        return List.of(typeOf(registry));
    }

    private List<AuthenticationHandlerSummary> authenticationHandlers() {
        val plan = authenticationEventExecutionPlan.getIfAvailable();
        if (plan == null) {
            return List.of();
        }
        return plan.resolveAuthenticationHandlers()
            .stream()
            .map(handler -> new AuthenticationHandlerSummary(handler.getName(), typeOf(handler),
                handler.getOrder(), handler.getState().name()))
            .sorted(Comparator.comparingInt(AuthenticationHandlerSummary::order).thenComparing(AuthenticationHandlerSummary::name))
            .toList();
    }

    private List<AuthenticationPolicySummary> authenticationPolicies() {
        val plan = authenticationEventExecutionPlan.getIfAvailable();
        if (plan == null) {
            return List.of();
        }
        return plan.getAuthenticationPolicies()
            .stream()
            .map(policy -> new AuthenticationPolicySummary(policy.getName(), typeOf(policy), policy.getOrder()))
            .sorted(Comparator.comparingInt(AuthenticationPolicySummary::order))
            .toList();
    }

    private List<AttributeRepositorySummary> attributeRepositories() {
        val plan = attributeRepositoryPlan.getIfAvailable();
        if (plan == null) {
            return List.of();
        }
        return plan.getAttributeRepositories()
            .stream()
            .map(CasDeploymentTools::summarize)
            .toList();
    }

    private List<McpToolGroup> mcpToolGroups() {
        return MCP_TOOL_GROUPS.entrySet()
            .stream()
            .map(entry -> new McpToolGroup(entry.getKey(),
                CasFeatureModule.FeatureCatalog.AI.toProperty(entry.getKey()),
                applicationContext.containsBean(entry.getValue())))
            .toList();
    }

    static AttributeRepositorySummary summarize(final PersonAttributeDao repository) {
        return new AttributeRepositorySummary(List.of(repository.getId()), typeOf(repository),
            repository.getOrder(), repository.isEnabled());
    }

    static String typeOf(final Object instance) {
        return ClassUtils.getUserClass(AopUtils.getTargetClass(instance)).getSimpleName();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Deployment(String casVersion, String serverName, @Nullable String serverPrefix, List<String> activeProfiles,
                             List<String> features, List<String> modules, String ticketRegistry, List<String> serviceRegistries,
                             List<AuthenticationHandlerSummary> authenticationHandlers,
                             List<AuthenticationPolicySummary> authenticationPolicies,
                             List<AttributeRepositorySummary> attributeRepositories,
                             List<McpToolGroup> mcpToolGroups) {
    }

    public record AuthenticationHandlerSummary(String name, String type, int order, String state) {
    }

    public record AuthenticationPolicySummary(String name, String type, int order) {
    }

    public record AttributeRepositorySummary(List<String> ids, String type, int order, boolean enabled) {
    }

    public record McpToolGroup(String feature, String property, boolean active) {
    }
}
