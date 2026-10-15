package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
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
     * Find the authorizable resources of a Heimdall namespace.
     *
     * @param namespace the namespace
     * @return the resources, or the known namespaces when the namespace is blank or unknown
     */
    @McpTool(description = "Find the resources Heimdall protects in a namespace: the URI pattern and HTTP method, or the "
        + "AuthZEN resource type, resource id pattern and actions, whether every policy must grant, the types of the "
        + "policies attached and the names of the resource properties. Policy definitions and property values are never "
        + "returned. Without a namespace, or for an unknown one, the known namespaces are listed.",
        title = "Find Authorizable Resources",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findAuthorizableResources(
        @McpToolParam(description = "The Heimdall namespace", required = false)
        final @Nullable String namespace) {
        val result = new LinkedHashMap<String, Object>();
        result.put("namespace", namespace);
        val repository = authorizableResourceRepository.getIfAvailable();
        if (repository == null) {
            result.put("reason", "Heimdall is not configured");
            return result;
        }
        val resources = StringUtils.isBlank(namespace) ? List.<AuthorizableResource>of() : repository.find(namespace);
        if (resources.isEmpty()) {
            if (StringUtils.isNotBlank(namespace)) {
                result.put("reason", "No resources are found in namespace " + namespace);
            }
            result.put("knownNamespaces", new TreeSet<>(repository.findAll().keySet()));
            return result;
        }
        val summaries = resources
            .stream()
            .sorted(Comparator.comparingLong(AuthorizableResource::getId))
            .map(CasHeimdallTools::summarize)
            .toList();
        result.put("count", summaries.size());
        result.put("resources", summaries);
        return result;
    }

    private static Map<String, Object> summarize(final AuthorizableResource resource) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("id", resource.getId());
        summary.put("pattern", resource.getPattern() != null ? resource.getPattern().pattern() : null);
        summary.put("method", resource.getMethod());
        summary.put("resourceType", resource.getResourceType());
        summary.put("resourceIdPattern", resource.getResourceIdPattern() != null ? resource.getResourceIdPattern().pattern() : null);
        summary.put("actions", new TreeSet<>(Objects.requireNonNullElseGet(resource.getActions(), Set::of)));
        summary.put("enforceAllPolicies", resource.isEnforceAllPolicies());
        summary.put("policies", Objects.requireNonNullElseGet(resource.getPolicies(), List::of)
            .stream()
            .map(policy -> AopUtils.getTargetClass(policy).getSimpleName())
            .toList());
        summary.put("propertyNames", new TreeSet<>(Objects.requireNonNullElseGet(resource.getProperties(), Map::of).keySet()));
        return summary;
    }
}
