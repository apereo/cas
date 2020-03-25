package org.apereo.cas.shell.commands.properties;

import org.apereo.cas.configuration.CasCoreConfigurationUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This is {@link AddPropertiesToConfigurationCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ShellCommandGroup("CAS Properties")
@ShellComponent
@Slf4j
public class AddPropertiesToConfigurationCommand {
    /**
     * Add properties to configuration.
     *
     * @param file  the file
     * @param group the group
     * @throws Exception the exception
     */
    @ShellMethod(key = "add-properties", value = "Add properties associated with a CAS group/module to a Properties/Yaml configuration file.")
    public static void add(
        @ShellOption(value = { "file", "--file" },
            help = "Path to the CAS configuration file",
            defaultValue = "/etc/cas/config/cas.properties") final String file,
        @ShellOption(value = { "group", "--group" },
            help = "Group/module whose associated settings should be added to the CAS configuration file") final String group) throws Exception {

        if (StringUtils.isBlank(file)) {
            LOGGER.warn("Configuration file must be specified");
            return;
        }

        val filePath = new File(file);
        if (filePath.exists() && (filePath.isDirectory() || !filePath.canRead() || !filePath.canWrite())) {
            LOGGER.warn("Configuration file [{}] is not readable/writable or is not a path to a file", filePath.getCanonicalPath());
            return;
        }

        val results = findProperties(group);
        LOGGER.info("Located [{}] properties matching [{}]", results.size(), group);

        switch (FilenameUtils.getExtension(filePath.getName()).toLowerCase()) {
            case "properties":
                createConfigurationFileIfNeeded(filePath);
                val props = loadPropertiesFromConfigurationFile(filePath);
                writeConfigurationPropertiesToFile(filePath, results, props);
                break;
            case "yaml":
            case "yml":
                createConfigurationFileIfNeeded(filePath);
                val yamlProps = CasCoreConfigurationUtils.loadYamlProperties(new FileSystemResource(filePath));
                writeYamlConfigurationPropertiesToFile(filePath, results, yamlProps);
                break;
            default:
                LOGGER.warn("Configuration file format [{}] is not recognized", filePath.getCanonicalPath());
        }

    }

    private static void writeYamlConfigurationPropertiesToFile(final File filePath,
                                                               final Map<String, ConfigurationMetadataProperty> results,
                                                               final Map<String, Object> yamlProps) throws Exception {
        val options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        options.setPrettyFlow(true);
        options.setAllowUnicode(true);
        val yaml = new Yaml(options);
        try (val writer = Files.newBufferedWriter(filePath.toPath(), StandardCharsets.UTF_8)) {
            putResultsIntoProperties(results, yamlProps);
            yaml.dump(yamlProps, writer);
        }
    }

    private static void writeConfigurationPropertiesToFile(final File filePath, final Map<String, ConfigurationMetadataProperty> results,
                                                           final Map<String, Object> p) throws Exception {
        LOGGER.info("Located [{}] properties in configuration file [{}]", results.size(), filePath.getCanonicalPath());
        putResultsIntoProperties(results, p);
        val lines = p.keySet().stream().map(s -> s + '=' + p.get(s))
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());
        FileUtils.writeLines(filePath, lines);
    }

    private static void putResultsIntoProperties(final Map<String, ConfigurationMetadataProperty> results, final Map<String, Object> p) {
        val lines = results.values().stream()
            .sorted(Comparator.comparing(ConfigurationMetadataProperty::getName))
            .collect(Collectors.toList());
        lines.forEach(v -> {
            val value = getDefaultValueForProperty(v);
            LOGGER.info("Adding property [{}={}]", v.getId(), value);
            p.put("# " + v.getId(), value);
        });
    }

    private static String getDefaultValueForProperty(final ConfigurationMetadataProperty v) {
        if (v.getDefaultValue() == null) {
            return StringUtils.EMPTY;
        }
        return v.getDefaultValue().toString();
    }

    private static Map loadPropertiesFromConfigurationFile(final File filePath) throws IOException {
        val p = new Properties();
        try (val f = Files.newBufferedReader(filePath.toPath(), StandardCharsets.UTF_8)) {
            p.load(f);
        }
        return p;
    }

    private static Map<String, ConfigurationMetadataProperty> findProperties(final String group) {
        val find = new FindPropertiesCommand();
        return find.findByProperty(group);
    }

    private static void createConfigurationFileIfNeeded(final File filePath) throws IOException {
        if (!filePath.exists()) {
            LOGGER.debug("Creating configuration file [{}]", filePath.getCanonicalPath());
            val created = filePath.createNewFile();
            if (created) {
                LOGGER.info("Created configuration file [{}]", filePath.getCanonicalPath());
            }
        }
    }
}
