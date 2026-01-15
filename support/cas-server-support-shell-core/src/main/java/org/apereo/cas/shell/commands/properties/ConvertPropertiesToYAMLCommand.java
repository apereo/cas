package org.apereo.cas.shell.commands.properties;

import module java.base;
import org.apereo.cas.shell.commands.CasShellCommand;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * This is {@link ConvertPropertiesToYAMLCommand}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class ConvertPropertiesToYAMLCommand implements CasShellCommand {
    /**
     * Convert properties to YAML.
     *
     * @param propertiesFile the properties file
     * @throws Exception the exception
     */
    @Command(group = "CAS Properties", name = "convert-props", description = "Convert CAS properties to YAML file at the same location.")
    public void convertProperties(
        @Option(
            longName = "properties",
            description = "Path to a properties file that contains CAS settings",
            defaultValue = "/etc/cas/config/cas.properties")
        final String propertiesFile) throws Exception {
        val output = FilenameUtils.removeExtension(propertiesFile) + ".yml";
        convertAndSaveToYaml(loadProperties(propertiesFile), output);
        LOGGER.info("Converted configuration properties to [{}]", output);
    }

    private static Map<String, Object> loadProperties(final String filePath) throws Exception {
        val properties = new Properties();
        try (val input = new FileInputStream(filePath)) {
            properties.load(input);

            val groupedProperties = new TreeMap<String, Object>();
            for (val key : properties.stringPropertyNames()) {
                addToGroupedProperties(groupedProperties, key, properties.getProperty(key));
            }
            return groupedProperties;
        }
    }

    private static void addToGroupedProperties(final Map<String, Object> groupedProperties,
                                               final String key, final String value) {
        val parts = Splitter.on('.').splitToList(key);
        var currentMap = groupedProperties;
        for (var i = 0; i < parts.size() - 1; i++) {
            currentMap = getOrCreateSubMap(currentMap, parts.get(i));
        }
        currentMap.put(parts.getLast(), value);
    }

    private static Map<String, Object> getOrCreateSubMap(final Map<String, Object> map, final String key) {
        return (Map<String, Object>) map.computeIfAbsent(key, _ -> new TreeMap<>());
    }

    private static void convertAndSaveToYaml(final Map<String, Object> properties,
                                             final String outputPath) throws Exception {
        val options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        options.setPrettyFlow(true);
        options.setAllowUnicode(true);
        options.setIndent(2);
        val yaml = new Yaml(options);
        try (val writer = new FileWriter(outputPath, StandardCharsets.UTF_8)) {
            yaml.dump(properties, writer);
        }
    }
}
