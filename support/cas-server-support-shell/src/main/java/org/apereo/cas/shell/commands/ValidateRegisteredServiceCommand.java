package org.apereo.cas.shell.commands;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * This is {@link ValidateRegisteredServiceCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Service
public class ValidateRegisteredServiceCommand implements CommandMarker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateRegisteredServiceCommand.class);
    private static final int SEP_LINE_LENGTH = 70;

    /**
     * Validate service.
     *
     * @param file      the file
     * @param directory the directory
     */
    @CliCommand(value = "validate-service", help = "Validate a given JSON/YAML service definition by path or directory")
    public void validateService(
            @CliOption(key = {"file"},
                    help = "Path to the JSON/YAML service definition file",
                    specifiedDefaultValue = "",
                    unspecifiedDefaultValue = "",
                    optionContext = "Path to the JSON/YAML service definition") final String file,
            @CliOption(key = {"directory"},
                    help = "Path to the JSON/YAML service definitions directory",
                    specifiedDefaultValue = "/etc/cas/services",
                    unspecifiedDefaultValue = "/etc/cas/services",
                    optionContext = "Path to the JSON/YAML service definitions directory") final String directory) {

        if (StringUtils.isBlank(file) && StringUtils.isBlank(directory)) {
            LOGGER.warn("Either file or directory must be specified");
            return;
        }

        if (StringUtils.isNotBlank(file)) {
            final File filePath = new File(file);
            validate(filePath);
            return;
        }
        if (StringUtils.isNotBlank(directory)) {
            final File directoryPath = new File(directory);
            if (directoryPath.isDirectory()) {
                FileUtils.listFiles(directoryPath, new String[]{"json", "yml"}, false).forEach(this::validate);
            }
            return;
        }

    }

    private void validate(final File filePath) {
        try {
            final DefaultRegisteredServiceJsonSerializer validator = new DefaultRegisteredServiceJsonSerializer();
            if (filePath.isFile() && filePath.exists() && filePath.canRead() && filePath.length() > 0) {
                final RegisteredService svc = validator.from(filePath);
                LOGGER.info("Service [{}] is valid at [{}].", svc.getName(), filePath.getCanonicalPath());
            } else {
                LOGGER.warn("File [{}] is does not exist, is not readable or is empty", filePath.getCanonicalPath());
            }
        } catch (final Exception e) {
            LOGGER.error("Could not understand and validate [{}]: [{}]", filePath.getPath(), e.getMessage());
        } finally {
            LOGGER.info(StringUtils.repeat('-', SEP_LINE_LENGTH));
        }
        
    }
}
