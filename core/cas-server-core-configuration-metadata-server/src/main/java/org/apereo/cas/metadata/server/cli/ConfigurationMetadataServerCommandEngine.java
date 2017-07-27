package org.apereo.cas.metadata.server.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.metadata.server.CasConfigurationMetadataRepository;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link ConfigurationMetadataServerCommandEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConfigurationMetadataServerCommandEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMetadataServerCommandEngine.class);
    private static final int SEP_LINE_LENGTH = 70;
    
    /**
     * Execute.
     *
     * @param args the args
     */
    public void execute(final String[] args) {
        final ConfigurationMetadataServerCommandLineParser parser = new ConfigurationMetadataServerCommandLineParser();
        final CommandLine line = parser.parse(args);
        if (args.length == 0 || parser.isHelp(line)) {
            parser.printHelp();
            return;
        }

        final boolean strict = parser.isStrictMatch(line);
        final Pattern groupPattern = parser.getGroup(line);
        final Pattern propertyPattern = parser.getProperty(line);
        
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
                        if (parser.isSummary(line)) {
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
