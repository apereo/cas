package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasAttributeTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasAttributeTools {
    private static final int MAX_DEFINITIONS = 100;

    private final ObjectProvider<PersonDirectoryAttributeRepositoryPlan> attributeRepositoryPlan;

    private final ObjectProvider<AttributeDefinitionStore> attributeDefinitionStore;

    /**
     * Ask each attribute repository separately which attributes it has for a person.
     *
     * @param username the username
     * @return the attribute names each repository returned, and repository errors
     */
    @McpTool(description = "Ask each configured attribute repository (LDAP, JDBC, REST, stub and so on) separately which "
        + "attributes it returns for a person, to find out where an attribute comes from or why it is missing. "
        + "Returns attribute names per repository and any error a repository raised; values are not returned, "
        + "use findPerson for the merged attributes and their values.",
        title = "Find Attribute Sources",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public AttributeSources findAttributeSources(
        @McpToolParam(description = "The username of the person")
        final String username) {
        val plan = attributeRepositoryPlan.getIfAvailable();
        if (plan == null || plan.isEmpty()) {
            return new AttributeSources(username, List.of(), "No attribute repositories are configured");
        }
        val sources = plan.getAttributeRepositories()
            .stream()
            .map(repository -> query(repository, username))
            .toList();
        return new AttributeSources(username, sources, null);
    }

    /**
     * Find attribute definitions.
     *
     * @param query the search text, optional
     * @return the matching attribute definitions
     */
    @McpTool(description = "List attribute definitions from the CAS attribute definition store, which rename, scope, "
        + "encrypt, hash or compute attributes before they are released. Matches the key, name or source attribute "
        + "against the given text, ignoring case; without text every definition is listed. Scripts are not returned, "
        + "only whether a definition is scripted. At most " + MAX_DEFINITIONS + " definitions are returned.",
        title = "Find Attribute Definitions",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public AttributeDefinitions findAttributeDefinitions(
        @McpToolParam(description = "Text to look for in the key, name or source attribute", required = false)
        @Nullable final String query) {
        val store = attributeDefinitionStore.getIfAvailable();
        val term = StringUtils.trimToEmpty(query);
        if (store == null) {
            return new AttributeDefinitions(term, 0, List.of(), "No attribute definition store is available");
        }
        val matches = store.getAttributeDefinitions()
            .stream()
            .filter(definition -> term.isEmpty()
                || Strings.CI.contains(definition.getKey(), term)
                || Strings.CI.contains(definition.getName(), term)
                || Strings.CI.contains(definition.getAttribute(), term))
            .sorted(Comparator.comparing(AttributeDefinition::getKey))
            .toList();
        val definitions = matches.stream().limit(MAX_DEFINITIONS).map(CasAttributeTools::summarize).toList();
        return new AttributeDefinitions(term, matches.size(), definitions, null);
    }

    private static AttributeSource query(final PersonAttributeDao repository, final String username) {
        val summary = CasDeploymentTools.summarize(repository);
        if (!repository.isEnabled()) {
            return new AttributeSource(summary.ids(), summary.type(), summary.order(), false, false, List.of(), null);
        }
        try {
            val person = repository.getPerson(username);
            val attributeNames = person != null && person.getAttributes() != null
                ? new TreeSet<>(person.getAttributes().keySet()).stream().toList()
                : List.<String>of();
            return new AttributeSource(summary.ids(), summary.type(), summary.order(), true, person != null, attributeNames, null);
        } catch (final Throwable e) {
            return new AttributeSource(summary.ids(), summary.type(), summary.order(), true, false, List.of(),
                Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()));
        }
    }

    private static AttributeDefinitionSummary summarize(final AttributeDefinition definition) {
        return new AttributeDefinitionSummary(definition.getKey(), definition.getName(), definition.getAttribute(),
            CasDeploymentTools.typeOf(definition), definition.isScoped(), definition.isEncrypted(), definition.isSingleValue(),
            StringUtils.isNotBlank(definition.getScript()), definition.getCanonicalizationMode(), definition.getHashingStrategy(),
            definition.getFlattened(), definition.getExpiration(),
            definition.getDependsOn() != null ? List.copyOf(definition.getDependsOn()) : List.of());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AttributeSources(String username, List<AttributeSource> repositories, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AttributeSource(List<String> ids, String type, int order, boolean enabled, boolean found,
                                  List<String> attributeNames, @Nullable String error) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AttributeDefinitions(String query, long count, List<AttributeDefinitionSummary> definitions, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AttributeDefinitionSummary(String key, @Nullable String name, @Nullable String attribute, String type,
                                             boolean scoped, boolean encrypted, boolean singleValue, boolean scripted,
                                             @Nullable String canonicalizationMode, @Nullable String hashingStrategy,
                                             @Nullable String flattened, @Nullable String expiration, List<String> dependsOn) {
    }
}
