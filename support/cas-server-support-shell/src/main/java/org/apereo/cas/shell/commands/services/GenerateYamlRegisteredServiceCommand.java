package org.apereo.cas.shell.commands.services;

import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.StringWriter;

/**
 * This is {@link GenerateYamlRegisteredServiceCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ShellCommandGroup("Registered Services")
@ShellComponent
@Slf4j
public class GenerateYamlRegisteredServiceCommand {

    private static final int SEP_LINE_LENGTH = 70;

    /**
     * Validate service.
     *
     * @param file        the file
     * @param destination the destination
     * @return the file
     */
    @ShellMethod(key = "generate-yaml", value = "Generate a YAML registered service definition")
    public static File generateYaml(
        @ShellOption(value = {"file", "--file"},
            help = "Path to the JSON service definition file") final String file,
        @ShellOption(value = {"destination", "--destination"},
            help = "Path to the destination YAML service definition file") final String destination) {
        if (StringUtils.isBlank(file)) {
            LOGGER.warn("File must be specified");
            return null;
        }

        val filePath = new File(file);
        val result = StringUtils.isBlank(destination) ? null : new File(destination);
        generate(filePath, result);
        return filePath;
    }

    private static void generate(final File filePath, final File result) {
        try {
            val validator = new RegisteredServiceJsonSerializer();
            if (filePath.isFile() && filePath.exists() && filePath.canRead() && filePath.length() > 0) {
                val svc = validator.from(filePath);
                LOGGER.info("Service [{}] is valid at [{}].", svc.getName(), filePath.getCanonicalPath());
                val yaml = new RegisteredServiceYamlSerializer();
                try (val writer = new StringWriter()) {
                    yaml.to(writer, svc);
                    LOGGER.info(writer.toString());

                    if (result != null) {
                        yaml.to(result, svc);
                        LOGGER.info("YAML service definition is saved at [{}].", result.getCanonicalPath());
                    }
                }
            } else {
                LOGGER.warn("File [{}] is does not exist, is not readable or is empty", filePath.getCanonicalPath());
            }
        } catch (final Exception e) {
            LOGGER.error("Could not understand and validate [{}]: [{}]", filePath.getPath(), e.getMessage());
        } finally {
            LOGGER.info("-".repeat(SEP_LINE_LENGTH));
        }

    }
}
