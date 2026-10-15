package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.mcp.resources.CasSettingResources;
import org.apereo.cas.metadata.CasConfigurationMetadataCatalog;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.CasReferenceProperty;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;
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
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public SettingSearch searchSettings(
        @McpToolParam(description = "The text to look for in setting names and descriptions, at least "
            + MIN_QUERY_LENGTH + " characters, such as ldap url or ticket timeout")
        final String query,
        @McpToolParam(description = "The CAS module that provides the settings, such as cas-server-support-ldap", required = false)
        final @Nullable String module) {
        val text = StringUtils.trimToEmpty(query);
        if (text.length() < MIN_QUERY_LENGTH) {
            return new SettingSearch(query, module, 0, false, List.of(), "Search for at least " + MIN_QUERY_LENGTH + " characters");
        }
        val repository = configurationMetadataRepository.getIfAvailable();
        if (repository == null) {
            return new SettingSearch(query, module, 0, false, List.of(), "The CAS settings catalog is not available");
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
        val summaries = settings
            .stream()
            .limit(MAX_RESULTS)
            .map(CasSettingTools::summarize)
            .toList();
        return new SettingSearch(query, module, settings.size(), settings.size() > MAX_RESULTS, summaries, null);
    }

    private static SettingSummary summarize(final CasReferenceProperty property) {
        val description = StringUtils.defaultIfBlank(property.getShortDescription(), property.getDescription());
        return new SettingSummary(property.getName(),
            StringUtils.abbreviate(StringUtils.normalizeSpace(description), MAX_DESCRIPTION_LENGTH),
            property.getModule(), property.getType(), StringUtils.isNotBlank(property.getDeprecationLevel()),
            StringUtils.replace(CasSettingResources.SETTING_URI_TEMPLATE, "{name}", property.getName()));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SettingSearch(String query, @Nullable String module, int count, boolean truncated, List<SettingSummary> settings,
                                @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SettingSummary(String name, @Nullable String description, @Nullable String module, @Nullable String type,
                                 boolean deprecated, String uri) {
    }
}
