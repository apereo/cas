package org.apereo.cas.shell.commands;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This is {@link AddPropertiesToConfigurationCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Service
public class AddPropertiesToConfigurationCommand implements CommandMarker {
    private static final Logger LOGGER = LoggerFactory.getLogger(FindPropertiesCommand.class);

    /**
     * Add properties to configuration.
     *
     * @param file  the file
     * @param group the group
     * @throws Exception the exception
     */
    @CliCommand(value = "add-properties", help = "Add properties associated with a CAS group/module to a Properties/Yaml configuration file.")
    public void add(@CliOption(key = {"file"},
            help = "Path to the CAS configuration file",
            unspecifiedDefaultValue = "/etc/cas/config/cas.properties",
            specifiedDefaultValue = "/etc/cas/config/cas.properties",
            optionContext = "Path to the CAS configuration file") final String file,
                    @CliOption(key = {"group"},
                            specifiedDefaultValue = "",
                            unspecifiedDefaultValue = "",
                            help = "Group/module whose associated settings should be added to the CAS configuration file",
                            optionContext = "Group/module whose associated settings should be added to the CAS configuration file",
                            mandatory = true) final String group) throws Exception {

        if (StringUtils.isBlank(file)) {
            LOGGER.warn("Configuration file must be specified");
            return;
        }

        final File filePath = new File(file);
        if (filePath.exists() && (filePath.isDirectory() || !filePath.canRead() || !filePath.canWrite())) {
            LOGGER.warn("Configuration file [{}] is not readable/writable or is not a path to a file", filePath.getCanonicalPath());
            return;
        }

        final Map<String, ConfigurationMetadataProperty> results = findProperties(group);
        LOGGER.info("Located [{}] properties matching [{}]", results.size(), group);

        switch (FilenameUtils.getExtension(filePath.getName()).toLowerCase()) {
            case "properties":
                createConfigurationFileIfNeeded(filePath);
                final Properties props = loadPropertiesFromConfigurationFile(filePath);
                writeConfigurationPropertiesToFile(filePath, results, props);
                break;
            case "yml":
                createConfigurationFileIfNeeded(filePath);
                final Properties yamlProps = loadYamlPropertiesFromConfigurationFile(filePath);
                writeYamlConfigurationPropertiesToFile(filePath, results, yamlProps);
                break;
            default:
                LOGGER.warn("Configuration file format [{}] is not recognized", filePath.getCanonicalPath());
        }
        
    }

    private void writeYamlConfigurationPropertiesToFile(final File filePath, final Map<String, ConfigurationMetadataProperty> results, 
                                                        final Properties yamlProps) throws Exception {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        options.setPrettyFlow(true);
        options.setAllowUnicode(true);
        final Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(filePath)) {
            putResultsIntoProperties(results, yamlProps);
            yaml.dump(yamlProps, writer);
        }
    }

    private Properties loadYamlPropertiesFromConfigurationFile(final File filePath) {
        final YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResolutionMethod(YamlProcessor.ResolutionMethod.OVERRIDE);
        factory.setResources(new FileSystemResource(filePath));
        factory.setSingleton(true);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    private void writeConfigurationPropertiesToFile(final File filePath, final Map<String, ConfigurationMetadataProperty> results, 
                                                    final Properties p) throws Exception {
        LOGGER.info("Located [{}] properties in configuration file [{}]", results.size(), filePath.getCanonicalPath());
        putResultsIntoProperties(results, p);
        final List<String> lines = p.stringPropertyNames().stream().map(s -> s + "=" + p.get(s)).collect(Collectors.toList());
        Collections.sort(lines, Comparator.naturalOrder());
        FileUtils.writeLines(filePath, lines);
    }

    private void putResultsIntoProperties(final Map<String, ConfigurationMetadataProperty> results, final Properties p) {
        final List<ConfigurationMetadataProperty> lines = results.values().stream().collect(Collectors.toList());
        Collections.sort(lines, Comparator.comparing(ConfigurationMetadataProperty::getName));
        
        lines.forEach(v -> {
            final String value;
            if (v.getDefaultValue() == null) {
                value = StringUtils.EMPTY;
            } else {
                value = v.getDefaultValue().toString();
            }
            LOGGER.info("Adding property [{}={}]", v.getId(), value);
            p.put("# " + v.getId(), value);
        });
    }

    private Properties loadPropertiesFromConfigurationFile(final File filePath) throws IOException {
        final Properties p = new Properties();
        try (FileReader f = new FileReader(filePath)) {
            p.load(f);
        }
        return p;
    }

    private Map<String, ConfigurationMetadataProperty> findProperties(final String group) {
        final FindPropertiesCommand find = new FindPropertiesCommand();
        final Map<String, ConfigurationMetadataProperty> results = find.findByProperty(group);
        return results;
    }

    private void createConfigurationFileIfNeeded(final File filePath) throws IOException {
        if (!filePath.exists()) {
            LOGGER.debug("Creating configuration file [{}]", filePath.getCanonicalPath());
            final boolean created = filePath.createNewFile();
            if (created) {
                LOGGER.info("Created configuration file [{}]", filePath.getCanonicalPath());
            }
        }
    }
}
