package org.apereo.cas.shell.commands.services;

import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;

/**
 * This is {@link ValidateRegisteredServiceCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ShellCommandGroup("Registered Services")
@ShellComponent
@Slf4j
public class ValidateRegisteredServiceCommand {

    private static final int SEP_LINE_LENGTH = 70;

    /**
     * Validate service.
     *
     * @param file      the file
     * @param directory the directory
     */
    @ShellMethod(key = "validate-service", value = "Validate a given JSON/YAML service definition by path or directory")
    public void validateService(
        @ShellOption(value = {"file"},
            help = "Path to the JSON/YAML service definition file") final String file,
        @ShellOption(value = {"directory"},
            help = "Path to the JSON/YAML service definitions directory",
            defaultValue = "/etc/cas/services") final String directory) {

        if (StringUtils.isBlank(file) && StringUtils.isBlank(directory)) {
            LOGGER.warn("Either file or directory must be specified");
            return;
        }

        if (StringUtils.isNotBlank(file)) {
            val filePath = new File(file);
            validate(filePath);
            return;
        }
        if (StringUtils.isNotBlank(directory)) {
            val directoryPath = new File(directory);
            if (directoryPath.isDirectory()) {
                FileUtils.listFiles(directoryPath, new String[]{"json", "yml"}, false).forEach(this::validate);
            }
            return;
        }

    }

    private void validate(final File filePath) {
        try {
            val validator = new DefaultRegisteredServiceJsonSerializer();
            if (filePath.isFile() && filePath.exists() && filePath.canRead() && filePath.length() > 0) {
                val svc = validator.from(filePath);
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
