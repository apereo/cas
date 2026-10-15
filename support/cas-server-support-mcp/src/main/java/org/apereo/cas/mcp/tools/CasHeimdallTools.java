package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.policy.ResourceAuthorizationPolicy;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasHeimdallTools}, available when the Heimdall authorization engine is included in the build.
 * Policies are reported by type only, since their definitions can carry connection details and credentials,
 * and resource properties by name only.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasHeimdallTools {
    private final ObjectProvider<AuthorizableResourceRepository> authorizableResourceRepository;

    /**
     * Find the resources Heimdall protects in a namespace. Policy definitions and property values are never returned.
     *
     * @param namespace the namespace
     * @return the resources, or the known namespaces
     */
    @McpTool(description = "Find the resources Heimdall protects in a namespace: the URI pattern and HTTP method, or the "
        + "AuthZEN resource type, resource id pattern and actions, whether every policy must grant, the types of the "
        + "policies attached and the names of the resource properties. Policy definitions and property values are never "
        + "returned. Without a namespace, or for an unknown one, the known namespaces are listed.",
        title = "Find Authorizable Resources",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public AuthorizableResources findAuthorizableResources(
        @McpToolParam(description = "The Heimdall namespace", required = false)
        final @Nullable String namespace) {
        val repository = authorizableResourceRepository.getIfAvailable();
        if (repository == null) {
            return new AuthorizableResources(namespace, 0, List.of(), null, "Heimdall is not configured");
        }
        val resources = StringUtils.isBlank(namespace) ? List.<AuthorizableResource>of() : repository.find(namespace);
        if (resources.isEmpty()) {
            val known = new TreeSet<>(repository.findAll().keySet()).stream().toList();
            return new AuthorizableResources(namespace, 0, List.of(), known,
                StringUtils.isNotBlank(namespace) ? "No resources are found in namespace " + namespace : null);
        }
        val summaries = resources
            .stream()
            .sorted(Comparator.comparingLong(AuthorizableResource::getId))
            .map(CasHeimdallTools::summarize)
            .toList();
        return new AuthorizableResources(namespace, summaries.size(), summaries, null, null);
    }

    private static AuthorizableResourceSummary summarize(final AuthorizableResource resource) {
        return new AuthorizableResourceSummary(resource.getId(),
            resource.getPattern() != null ? resource.getPattern().pattern() : null,
            resource.getMethod(), resource.getResourceType(),
            resource.getResourceIdPattern() != null ? resource.getResourceIdPattern().pattern() : null,
            new TreeSet<>(Objects.requireNonNullElseGet(resource.getActions(), Set::<String>of)).stream().toList(),
            resource.isEnforceAllPolicies(),
            Objects.requireNonNullElseGet(resource.getPolicies(), List::<ResourceAuthorizationPolicy>of)
                .stream()
                .map(policy -> AopUtils.getTargetClass(policy).getSimpleName())
                .toList(),
            new TreeSet<>(Objects.requireNonNullElseGet(resource.getProperties(), Map::<String, Object>of).keySet()).stream().toList());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AuthorizableResources(@Nullable String namespace, int count, List<AuthorizableResourceSummary> resources,
                                        @Nullable List<String> knownNamespaces, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AuthorizableResourceSummary(long id, @Nullable String pattern, @Nullable String method, @Nullable String resourceType,
                                              @Nullable String resourceIdPattern, List<String> actions, boolean enforceAllPolicies,
                                              List<String> policies, List<String> propertyNames) {
    }
}
