package org.apereo.cas.metadata.server.shell.commands;

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
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link FindCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Service
public class FindCommand implements CommandMarker {
    private static final Logger LOGGER = LoggerFactory.getLogger(FindCommand.class);
    private static final int SEP_LINE_LENGTH = 70;

    /**
     * Find property.
     *
     * @param name    the name
     * @param group   the group
     * @param strict  the strict match
     * @param summary the summary
     * @return the string
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

        find(strict, summary, RegexUtils.createPattern(group), RegexUtils.createPattern(name));
    }

    /**
     * Find.
     *
     * @param strict          the strict
     * @param summary         the summary
     * @param groupPattern    the group pattern
     * @param propertyPattern the property pattern
     */
    public void find(final boolean strict, final boolean summary,
                     final Pattern groupPattern, final Pattern propertyPattern) {
        final CasConfigurationMetadataRepository repository = new CasConfigurationMetadataRepository();
        final Collection<ConfigurationMetadataGroup> groups = repository.getRepository().getAllGroups()
                .entrySet()
                .stream()
                .filter(k -> strict ? RegexUtils.matches(groupPattern, k.getKey()) : RegexUtils.find(groupPattern, k.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        groups
                .stream()
                .filter(g -> g.getProperties()
                        .entrySet()
                        .stream()
                        .filter(k -> strict ? RegexUtils.matches(propertyPattern, k.getKey()) : RegexUtils.find(propertyPattern, k.getKey()))
                        .findAny()
                        .isPresent()
                )
                .forEach(g -> {
                    final Map<String, ConfigurationMetadataProperty> p = g.getProperties();
                    p.entrySet().forEach(s -> {
                        if (summary) {
                            LOGGER.info("{}={}", s.getKey(), s.getValue().getDefaultValue());
                            LOGGER.info("{}", StringUtils.normalizeSpace(s.getValue().getShortDescription()));
                        } else {
                            LOGGER.info("Property: {}", s.getKey());
                            LOGGER.info("Group: {}", g.getId());
                            LOGGER.info("Default Value: {}", s.getValue().getDefaultValue());
                            LOGGER.info("Type: {}", s.getValue().getType());
                            LOGGER.info("Summary: {}", StringUtils.normalizeSpace(s.getValue().getShortDescription()));
                            LOGGER.info("Description: {}", StringUtils.normalizeSpace(s.getValue().getDescription()));
                            LOGGER.info("Deprecated: {}", Boolean.toString(s.getValue().isDeprecated()));
                        }
                        LOGGER.info(StringUtils.repeat('-', SEP_LINE_LENGTH));
                    });
                });
    }
}
