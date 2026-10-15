package org.apereo.cas.mcp.resources;

import module java.base;
import org.apereo.cas.metadata.CasConfigurationMetadataCatalog;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.CasReferenceProperty;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.ai.mcp.annotation.McpComplete;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import tools.jackson.databind.json.JsonMapper;

/**
 * This is {@link CasSettingResources}. It offers the CAS settings catalog as an MCP resource template, so that clients can
 * read what a setting means: its description, type, default value, the module that provides it and whether it is
 * deprecated. Whether the setting is defined in this deployment is reported, but its value never is, since settings carry
 * keys, passwords and other secrets. Setting names are completed from at least two typed characters.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasSettingResources {
    /**
     * The URI template of a CAS setting.
     */
    public static final String SETTING_URI_TEMPLATE = "cas://settings/{name}";

    private static final int MAX_COMPLETIONS = 100;

    private static final int MIN_COMPLETION_LENGTH = 2;

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final ObjectProvider<CasConfigurationMetadataRepository> configurationMetadataRepository;

    private final Environment environment;

    /**
     * Describe a CAS setting.
     *
     * @param name the setting name, such as {@code cas.server.name}
     * @return the setting description as JSON
     */
    @McpResource(uri = SETTING_URI_TEMPLATE, name = "casSetting", title = "CAS Setting",
        description = "Describe a CAS or Spring Boot setting by name, such as cas.server.name: what it does, its type and "
            + "default value, the CAS module that provides it, whether it is required, deprecated or accepts durations, "
            + "regular expressions or expressions, and whether it is defined in this deployment. Values are never returned.",
        mimeType = "application/json")
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public String describeSetting(final String name) {
        val result = new LinkedHashMap<String, Object>();
        result.put("name", name);
        val repository = configurationMetadataRepository.getIfAvailable();
        if (repository == null) {
            result.put("reason", "The CAS settings catalog is not available");
            return toJson(result);
        }
        val canonicalName = ConfigurationPropertyName.isValid(name) ? ConfigurationPropertyName.of(name).toString() : name;
        val query = ConfigurationMetadataCatalogQuery.builder()
            .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.ALL)
            .queryFilter(property -> property.getId().equals(name) || property.getId().equals(canonicalName))
            .build();
        val found = CasConfigurationMetadataCatalog.query(query, repository).properties()
            .stream()
            .findFirst();
        found.ifPresentOrElse(property -> {
            result.put("name", property.getName());
            result.put("setting", summarize(property));
        }, () -> result.put("reason", "No setting is named " + name));
        result.put("defined", environment.containsProperty(found.map(CasReferenceProperty::getName).orElse(name)));
        return toJson(result);
    }

    /**
     * Complete setting names.
     *
     * @param value the setting name typed so far
     * @return setting names that start with it, then setting names that contain it
     */
    @McpComplete(uri = SETTING_URI_TEMPLATE)
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public McpSchema.CompleteResult.CompleteCompletion completeSettingName(final String value) {
        val repository = configurationMetadataRepository.getIfAvailable();
        val term = StringUtils.trimToEmpty(value);
        if (repository == null || term.length() < MIN_COMPLETION_LENGTH) {
            return new McpSchema.CompleteResult.CompleteCompletion(List.of(), 0, false);
        }
        val query = ConfigurationMetadataCatalogQuery.builder()
            .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.ALL)
            .queryFilter(property -> Strings.CI.contains(property.getId(), term))
            .build();
        val names = CasConfigurationMetadataCatalog.query(query, repository).properties()
            .stream()
            .map(CasReferenceProperty::getName)
            .distinct()
            .sorted(Comparator.comparing((String name) -> !Strings.CI.startsWith(name, term)).thenComparing(Comparator.<String>naturalOrder()))
            .toList();
        return new McpSchema.CompleteResult.CompleteCompletion(names.stream().limit(MAX_COMPLETIONS).toList(),
            names.size(), names.size() > MAX_COMPLETIONS);
    }

    private static Map<String, Object> summarize(final CasReferenceProperty property) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("description", property.getDescription());
        summary.put("type", property.getType());
        summary.put("defaultValue", property.getDefaultValue());
        summary.put("module", property.getModule());
        summary.put("owner", property.getOwner());
        summary.put("required", property.isRequired());
        summary.put("duration", property.isDuration());
        summary.put("regularExpression", property.isRegexPattern());
        summary.put("expressionLanguage", property.isExpressionLanguage());
        summary.put("deprecationLevel", property.getDeprecationLevel());
        summary.put("deprecationReason", property.getDeprecationReason());
        summary.put("deprecationReplacement", property.getDeprecationReplacement());
        return summary;
    }

    private static String toJson(final Map<String, Object> result) {
        return MAPPER.writeValueAsString(result);
    }
}
