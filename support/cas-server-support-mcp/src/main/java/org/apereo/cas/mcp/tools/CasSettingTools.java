package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.mcp.resources.CasSettingResources;
import org.apereo.cas.metadata.CasConfigurationMetadataCatalog;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.CasReferenceProperty;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;
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
 * This is {@link CasSettingTools}, which searches the CAS settings catalog so that clients can find the name of a setting
 * and then read it through the {@code cas://settings/{name}} resource. Setting values are never returned.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasSettingTools {
    private static final int MAX_RESULTS = 50;

    private static final int MIN_QUERY_LENGTH = 3;

    private static final int MAX_DESCRIPTION_LENGTH = 200;

    private final ObjectProvider<CasConfigurationMetadataRepository> configurationMetadataRepository;

    /**
     * Search CAS settings by name or description.
     *
     * @param query  the text to look for
     * @param module the optional CAS module the settings belong to
     * @return the matching settings
     */
    @McpTool(description = "Search CAS and Spring Boot settings whose name or description contains the text, optionally "
        + "only those provided by a CAS module such as cas-server-support-ldap, and return their name, a short "
        + "description, module, type and deprecation, with the cas://settings/{name} resource URI that describes each "
        + "in full. At most " + MAX_RESULTS + " settings are returned. Setting values are never returned.",
        title = "Search Settings",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> searchSettings(
        @McpToolParam(description = "The text to look for in setting names and descriptions, at least "
            + MIN_QUERY_LENGTH + " characters, such as ldap url or ticket timeout")
        final String query,
        @McpToolParam(description = "The CAS module that provides the settings, such as cas-server-support-ldap", required = false)
        final @Nullable String module) {
        val result = new LinkedHashMap<String, Object>();
        result.put("query", query);
        result.put("module", module);
        val text = StringUtils.trimToEmpty(query);
        if (text.length() < MIN_QUERY_LENGTH) {
            result.put("reason", "Search for at least " + MIN_QUERY_LENGTH + " characters");
            return result;
        }
        val repository = configurationMetadataRepository.getIfAvailable();
        if (repository == null) {
            result.put("reason", "The CAS settings catalog is not available");
            return result;
        }
        val terms = Arrays.stream(StringUtils.split(text)).toList();
        val builder = ConfigurationMetadataCatalogQuery.builder()
            .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.ALL)
            .queryFilter(property -> terms.stream().allMatch(term -> Strings.CI.contains(property.getId(), term)
                || Strings.CI.contains(property.getDescription(), term)));
        if (StringUtils.isNotBlank(module)) {
            builder.modules(List.of(module.trim()));
        }
        val settings = CasConfigurationMetadataCatalog.query(builder.build(), repository).properties();
        result.put("count", settings.size());
        result.put("truncated", settings.size() > MAX_RESULTS);
        result.put("settings", settings
            .stream()
            .limit(MAX_RESULTS)
            .map(CasSettingTools::summarize)
            .toList());
        return result;
    }

    private static Map<String, Object> summarize(final CasReferenceProperty property) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("name", property.getName());
        val description = StringUtils.defaultIfBlank(property.getShortDescription(), property.getDescription());
        summary.put("description", StringUtils.abbreviate(StringUtils.normalizeSpace(description), MAX_DESCRIPTION_LENGTH));
        summary.put("module", property.getModule());
        summary.put("type", property.getType());
        summary.put("deprecated", StringUtils.isNotBlank(property.getDeprecationLevel()));
        summary.put("uri", StringUtils.replace(CasSettingResources.SETTING_URI_TEMPLATE, "{name}", property.getName()));
        return summary;
    }
}
