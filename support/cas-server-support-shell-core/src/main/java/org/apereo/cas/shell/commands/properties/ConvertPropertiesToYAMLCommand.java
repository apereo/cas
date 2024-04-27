package org.apereo.cas.shell.commands.properties;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * This is {@link ConvertPropertiesToYAMLCommand}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ShellCommandGroup("CAS Properties")
@ShellComponent
@Slf4j
public class ConvertPropertiesToYAMLCommand {
    /**
     * Convert properties to YAML.
     *
     * @param propertiesFile the properties file
     * @throws Exception the exception
     */
    @ShellMethod(key = "convert-props", value = "Convert CAS properties to YAML file at the same location.")
    public void convertProperties(
        @ShellOption(value = {"properties", "--properties"},
            help = "Path to a properties file that contains CAS settings",
            defaultValue = "/etc/cas/config/cas.properties") final String propertiesFile) throws Exception {
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
        return (Map<String, Object>) map.computeIfAbsent(key, __ -> new TreeMap<>());
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
