package org.apereo.cas.metadata.rest;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.core.Ordered;
import jakarta.annotation.Nonnull;
import java.io.Serial;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * This is {@link ConfigurationMetadataSearchResult}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode(of = {"order", "group"}, callSuper = true)
public class ConfigurationMetadataSearchResult extends ConfigurationMetadataProperty implements Ordered, Comparable<ConfigurationMetadataSearchResult> {

    @Serial
    private static final long serialVersionUID = 7767348341760984539L;

    private static final Pattern PATTERN_DESCRIPTION_CODE = RegexUtils.createPattern("\\{@code (.+)\\}");

    private static final Pattern PATTERN_DESCRIPTION_SEE = RegexUtils.createPattern("@see (.+)");

    private static final Pattern PATTERN_DESCRIPTION_LINK = RegexUtils.createPattern("\\{@link (.+)\\}");

    private int order;

    private String group;

    private boolean requiredProperty;

    private String requiredModule;

    private boolean requiredModuleAutomated;

    public ConfigurationMetadataSearchResult(final ConfigurationMetadataProperty prop) {
        try {
            setDefaultValue(prop.getDefaultValue());
            setDeprecation(prop.getDeprecation());
            setDescription(cleanUpDescription(prop.getDescription()));
            setShortDescription(cleanUpDescription(prop.getShortDescription()));
            setId(prop.getId());
            setName(prop.getName());
            setType(prop.getType());
            setGroup(CasConfigurationMetadataRepository.getPropertyGroupId(prop));
            setOrder(CasConfigurationMetadataRepository.isCasProperty(prop) ? Ordered.HIGHEST_PRECEDENCE : Ordered.LOWEST_PRECEDENCE);
            val valueHints = prop.getHints().getValueHints();
            valueHints.forEach(hint -> {
                val values = CollectionUtils.toCollection(hint.getValue());
                if (values.contains(RequiresModule.class.getName())) {
                    setRequiredModule(hint.getDescription());
                    setRequiredModuleAutomated(values.contains(Boolean.TRUE));
                }
                if (values.contains(RequiredProperty.class.getName())) {
                    setRequiredProperty(true);
                }
            });
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    private static String cleanUpDescription(final String propDescription) {
        var description = propDescription;
        val format = "<pre>%s</pre>";
        if (StringUtils.isNotBlank(description)) {
            var matcher = PATTERN_DESCRIPTION_CODE.matcher(description);
            if (matcher.find()) {
                description = RegExUtils.replacePattern((CharSequence) description, PATTERN_DESCRIPTION_CODE.pattern(), String.format(format, matcher.group(1)));
            }
            matcher = PATTERN_DESCRIPTION_LINK.matcher(description);
            if (matcher.find()) {
                val replacement = "See ".concat(String.format(format, matcher.group(1)));
                description = RegExUtils.replacePattern((CharSequence) description, PATTERN_DESCRIPTION_LINK.pattern(), replacement);
            }
            matcher = PATTERN_DESCRIPTION_SEE.matcher(description);
            if (matcher.find()) {
                val replacement = "See ".concat(String.format(format, matcher.group(1)));
                description = RegExUtils.replacePattern((CharSequence) description, PATTERN_DESCRIPTION_SEE.pattern(), replacement);
            }
            return description;
        }
        return propDescription;
    }


    @Override
    public int compareTo(@Nonnull final ConfigurationMetadataSearchResult result) {
        return Comparator.comparingInt(ConfigurationMetadataSearchResult::getOrder)
            .thenComparing(ConfigurationMetadataSearchResult::getName)
            .thenComparing(ConfigurationMetadataSearchResult::getGroup)
            .compare(this, result);
    }
}
