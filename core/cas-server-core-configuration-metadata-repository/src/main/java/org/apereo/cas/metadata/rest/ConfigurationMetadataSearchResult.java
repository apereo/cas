package org.apereo.cas.metadata.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link ConfigurationMetadataSearchResult}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConfigurationMetadataSearchResult extends ConfigurationMetadataProperty implements Ordered, Comparable<ConfigurationMetadataSearchResult> {
    private static final long serialVersionUID = 7767348341760984539L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMetadataSearchResult.class);

    private static final Pattern PATTERN_DESCRIPTION_CODE = RegexUtils.createPattern("\\{@code (.+)\\}");
    private static final Pattern PATTERN_DESCRIPTION_SEE = RegexUtils.createPattern("@see (.+)");
    private static final Pattern PATTERN_DESCRIPTION_LINK = RegexUtils.createPattern("\\{@link (.+)\\}");

    private int order;
    private String group;
    private boolean requiredProperty;
    private String requiredModule;
    private boolean requiredModuleAutomated;

    public ConfigurationMetadataSearchResult(final ConfigurationMetadataProperty prop, final CasConfigurationMetadataRepository repository) {
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

            final List<ValueHint> valueHints = prop.getHints().getValueHints();

            valueHints.forEach(hint -> {
                final Set values = CollectionUtils.toCollection(hint.getValue());
                if (values.contains(RequiresModule.class.getName())) {
                    setRequiredModule(hint.getDescription());
                    setRequiredModuleAutomated(values.contains(Boolean.TRUE));
                }
                if (values.contains(RequiredProperty.class.getName())) {
                    setRequiredProperty(true);
                }
            });

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public String getRequiredModule() {
        return requiredModule;
    }

    public void setRequiredModule(final String requiredModule) {
        this.requiredModule = requiredModule;
    }

    public boolean isRequiredProperty() {
        return requiredProperty;
    }

    public void setRequiredProperty(final boolean requiredProperty) {
        this.requiredProperty = requiredProperty;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public boolean isRequiredModuleAutomated() {
        return requiredModuleAutomated;
    }

    public void setRequiredModuleAutomated(final boolean requiredModuleAutomated) {
        this.requiredModuleAutomated = requiredModuleAutomated;
    }

    private String cleanUpDescription(final String propDescription) {
        String description = propDescription;
        final String format = "<code>%s</code>";
        if (StringUtils.isNotBlank(description)) {
            Matcher matcher = PATTERN_DESCRIPTION_CODE.matcher(description);
            if (matcher.find()) {
                description = StringUtils.replacePattern(description, PATTERN_DESCRIPTION_CODE.pattern(), String.format(format, matcher.group(1)));
            }
            matcher = PATTERN_DESCRIPTION_LINK.matcher(description);
            if (matcher.find()) {
                final String replacement = "See ".concat(String.format(format, matcher.group(1)));
                description = StringUtils.replacePattern(description, PATTERN_DESCRIPTION_LINK.pattern(), replacement);
            }

            matcher = PATTERN_DESCRIPTION_SEE.matcher(description);
            if (matcher.find()) {
                final String replacement = "See ".concat(String.format(format, matcher.group(1)));
                description = StringUtils.replacePattern(description, PATTERN_DESCRIPTION_SEE.pattern(), replacement);
            }
            return description;
        }
        return propDescription;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final ConfigurationMetadataSearchResult rhs = (ConfigurationMetadataSearchResult) obj;
        return new EqualsBuilder()
                .append(this.order, rhs.order)
                .append(getName(), rhs.getName())
                .append(this.group, rhs.group)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(order)
                .append(getName())
                .append(group)
                .toHashCode();
    }

    @Override
    public int compareTo(final ConfigurationMetadataSearchResult o) {
        return new CompareToBuilder()
                .append(this.order, o.getOrder())
                .append(getName(), o.getName())
                .append(this.group, o.getGroup())
                .build();
    }
}
