package org.apereo.cas.shell.commands;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link FindPropertiesCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Service
public class FindPropertiesCommand implements CommandMarker {
    private static final Logger LOGGER = LoggerFactory.getLogger(FindPropertiesCommand.class);
    private static final int SEP_LINE_LENGTH = 70;

    /**
     * Find property.
     *
     * @param name    the name
     * @param group   the group
     * @param strict  the strict match
     * @param summary the summary
     */
    @CliCommand(value = "find", help = "Look up properties associated with a CAS group/module.")
    public void find(
            @CliOption(key = {"name"},
                    help = "Property name regex pattern",
                    optionContext = "Property name regex pattern",
                    specifiedDefaultValue = ".+",
                    unspecifiedDefaultValue = ".+") final String name,
            @CliOption(key = {"group"},
                    help = "Group/module regex pattern that is associated with the property",
                    optionContext = "Group/module regex pattern that is associated with the property",
                    specifiedDefaultValue = ".+",
                    unspecifiedDefaultValue = ".+") final String group,
            @CliOption(key = {"strict-match"},
                    help = "Whether pattern should be done in strict-mode which means "
                            + "the matching engine tries to match the entire region for the query.",
                    optionContext = "Whether pattern should be done in strict-mode which means "
                            + "the matching engine tries to match the entire region for the query.",
                    unspecifiedDefaultValue = "false",
                    specifiedDefaultValue = "true") final boolean strict,
            @CliOption(key = {"summary"},
                    help = "Whether results should be presented in summarized mode",
                    optionContext = "Whether results should be presented in summarized mode",
                    unspecifiedDefaultValue = "false",
                    specifiedDefaultValue = "true") final boolean summary) {

        final Map<String, ConfigurationMetadataProperty> results = find(strict, summary, RegexUtils.createPattern(group), RegexUtils.createPattern(name));

        if (results.isEmpty()) {
            LOGGER.info("Could not find any results matching the criteria");
            return;
        }

        results.forEach((k, v) -> {
            if (summary) {
                LOGGER.info("{}={}", k, v.getDefaultValue());
                LOGGER.info("{}", StringUtils.normalizeSpace(v.getShortDescription()));
            } else {
                LOGGER.info("Property: {}", k);
                /*
                final String relaxedName = StreamSupport.stream(RelaxedNames.forCamelCase(k).spliterator(), false)
                        .map(Object::toString)
                        .collect(Collectors.joining(","));
                LOGGER.info("Synonyms: {}", relaxedName);
                */
                LOGGER.info("Group: {}", StringUtils.substringBeforeLast(k, "."));
                LOGGER.info("Default Value: {}", ObjectUtils.defaultIfNull(v.getDefaultValue(), "[blank]"));
                LOGGER.info("Type: {}", v.getType());
                LOGGER.info("Summary: {}", StringUtils.normalizeSpace(v.getShortDescription()));
                LOGGER.info("Description: {}", StringUtils.normalizeSpace(v.getDescription()));
                LOGGER.info("Deprecated: {}", BooleanUtils.toStringYesNo(v.isDeprecated()));
            }
            LOGGER.info(StringUtils.repeat('-', SEP_LINE_LENGTH));
        });
    }

    /**
     * Find.
     *
     * @param strict          the strict
     * @param summary         the summary
     * @param groupPattern    the group pattern
     * @param propertyPattern the property pattern
     * @return the map
     */
    public Map<String, ConfigurationMetadataProperty> find(final boolean strict, final boolean summary,
                                                           final Pattern groupPattern, final Pattern propertyPattern) {
        final Map<String, ConfigurationMetadataProperty> results = new LinkedHashMap<>();

        final CasConfigurationMetadataRepository repository = new CasConfigurationMetadataRepository();
        final Collection<ConfigurationMetadataGroup> groups = repository.getRepository().getAllGroups()
                .entrySet()
                .stream()
                .filter(k -> strict ? RegexUtils.matches(groupPattern, k.getKey()) : RegexUtils.find(groupPattern, k.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        groups.forEach(g -> g.getProperties().forEach((k, v) -> {
            final boolean matched = strict ? RegexUtils.matches(propertyPattern, k) : RegexUtils.find(propertyPattern, k);
            if (matched) {
                results.put(k, v);
            }
        }));

        return results;
    }

    /**
     * Find by group.
     *
     * @param name the name
     * @return the map
     */
    public Map<String, ConfigurationMetadataProperty> findByProperty(final String name) {
        return find(false, false, RegexUtils.createPattern(".+"), RegexUtils.createPattern(name));
    }
}
