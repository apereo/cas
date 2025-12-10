package org.apereo.cas.shell.commands.properties;

import org.apereo.cas.configuration.support.RelaxedPropertyNames;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.shell.commands.CasShellCommand;
import org.apereo.cas.util.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * This is {@link FindPropertiesCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class FindPropertiesCommand implements CasShellCommand {

    private static final int SEP_LINE_LENGTH = 70;

    /**
     * Find property.
     *
     * @param name    the name
     * @param strict  the strict match
     * @param summary the summary
     */
    @Command(group = "CAS Properties", name = "find", description = "Look up properties associated with a CAS group/module.")
    public void find(
        @Option(
            longName = "name",
            description = "Property name regex pattern",
            defaultValue = ".+")
        final String name,
        @Option(
            longName = "strict",
            defaultValue = "false",
            description = "Whether pattern should be done in strict-mode which means "
                + "the matching engine tries to match the entire region for the query.")
        final boolean strict,
        @Option(
            longName = "summary",
            defaultValue = "false",
            description = "Whether results should be presented in summarized mode")
        final boolean summary) {

        val results = find(strict, RegexUtils.createPattern(name));

        if (results.isEmpty()) {
            LOGGER.info("Could not find any results matching the criteria");
            return;
        }

        results.forEach((k, v) -> {
            if (summary) {
                LOGGER.info("[{}]=[{}]", k, v.getDefaultValue());
                val value = StringUtils.normalizeSpace(v.getShortDescription());
                if (StringUtils.isNotBlank(value)) {
                    LOGGER.info("[{}]", value);
                }
            } else {
                LOGGER.info("Property: [{}]", k);
                LOGGER.info("Group: [{}]", StringUtils.substringBeforeLast(k, "."));
                LOGGER.info("Default Value: [{}]", ObjectUtils.getIfNull(v.getDefaultValue(), "[blank]"));
                LOGGER.info("Type: [{}]", v.getType());
                LOGGER.info("Summary: [{}]", StringUtils.normalizeSpace(v.getShortDescription()));
                LOGGER.info("Description: [{}]", StringUtils.normalizeSpace(v.getDescription()));
                LOGGER.info("Deprecated: [{}]", BooleanUtils.toStringYesNo(v.isDeprecated()));
            }
            LOGGER.info("-".repeat(SEP_LINE_LENGTH));
        });
    }

    /**
     * Find.
     *
     * @param strict          the strict
     * @param propertyPattern the property pattern
     * @return the map
     */
    public Map<String, ConfigurationMetadataProperty> find(final boolean strict, final Pattern propertyPattern) {
        val results = new HashMap<String, ConfigurationMetadataProperty>();

        val repository = new CasConfigurationMetadataRepository();
        val props = repository.getRepository().getAllProperties();

        props.forEach((k, v) -> {
            val matched = StreamSupport.stream(RelaxedPropertyNames.forCamelCase(k).spliterator(), false)
                .map(Object::toString)
                .anyMatch(name -> strict
                    ? RegexUtils.matches(propertyPattern, name)
                    : RegexUtils.find(propertyPattern, name));
            if (matched) {
                results.put(k, v);
            }
        });

        return results;
    }

    /**
     * Find by group.
     *
     * @param name the name
     * @return the map
     */
    public Map<String, ConfigurationMetadataProperty> findByProperty(final String name) {
        return find(false, RegexUtils.createPattern(name));
    }
}
