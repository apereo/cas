package org.apereo.cas.shell.commands;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
    @CliCommand(value = "add-properties", help = "Look up properties associated with a CAS group/module.")
    public void add(@CliOption(key = {"file"},
            help = "Path to the CAS configuration file",
            unspecifiedDefaultValue = "/etc/cas/config/cas-delme.properties",
            specifiedDefaultValue = "/etc/cas/config/cas-delme.properties",
            optionContext = "Path to the CAS configuration file") final String file,
                    @CliOption(key = {"group"},
                            specifiedDefaultValue = "",
                            unspecifiedDefaultValue = "",
                            help = "Group/module whose associated settings should be added to the configuration file",
                            optionContext = "Group/module whose associated settings should be added to the configuration file",
                            mandatory = true) final String group)
            throws Exception {

        if (StringUtils.isBlank(file)) {
            LOGGER.warn("Configuration file must be specified");
            return;
        }

        final File filePath = new File(file);
        if (filePath.exists() && (filePath.isDirectory() || !filePath.canRead() || !filePath.canWrite())) {
            LOGGER.warn("Configuration file [{}] is not readable/writable or is not a path to a file", filePath.getCanonicalPath());
            return;
        }
        
        switch (FilenameUtils.getExtension(filePath.getName()).toLowerCase()) {
            case "properties":
                final Map<String, ConfigurationMetadataProperty> results = findProperties(group);
                if (!results.isEmpty()) {
                    createConfigurationFileIfNeeded(filePath);
                    final Properties p = new Properties();
                    p.load(new FileReader(filePath));
                    LOGGER.info("Located [{}] properties in configuration file [{}]", filePath);
                    results.forEach((k, v) -> {
                        LOGGER.info("Adding property [{}]", v.getName());
                        final String value;
                        if (v.getDefaultValue() == null) {
                            value = StringUtils.EMPTY;
                        } else {
                            value = v.getDefaultValue().toString();
                        }
                        p.put("# " + v.getName(), value);
                    });
                    final List<String> lines = p.stringPropertyNames().stream().map(s -> s + "=" + p.get(s)).collect(Collectors.toList());
                    Collections.sort(lines, Comparator.naturalOrder());
                    FileUtils.writeLines(filePath, lines);
                }
                break;
            case "yml":
                createConfigurationFileIfNeeded(filePath);
                break;
            default:
                LOGGER.warn("Unknown configuration file format: [{}]", filePath.getCanonicalPath());
        }
    }

    private Map<String, ConfigurationMetadataProperty> findProperties(final String group) {
        final FindPropertiesCommand find = new FindPropertiesCommand();
        final Map<String, ConfigurationMetadataProperty> results = find.findByProperty(group);
        LOGGER.info("Located [{}] properties matching [{}]", results.size(), group);
        return results;
    }

    private void createConfigurationFileIfNeeded(final File filePath) throws IOException {
        if (!filePath.exists()) {
            LOGGER.info("Creating configuration file [{}]", filePath.getCanonicalPath());
            filePath.createNewFile();
        }
    }
}
