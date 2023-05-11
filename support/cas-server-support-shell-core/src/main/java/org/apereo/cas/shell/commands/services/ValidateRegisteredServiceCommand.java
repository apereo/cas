package org.apereo.cas.shell.commands.services;

import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.Objects;

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

    @Autowired
    private ConfigurableApplicationContext applicationContext;


    /**
     * Validate service.
     *
     * @param file      the file
     * @param directory the directory
     */
    @ShellMethod(key = "validate-service", value = "Validate a given JSON/YAML service definition by path or directory")
    public void validateService(
        @ShellOption(value = {"file", "--file"},
            help = "Path to the JSON/YAML service definition file",
            defaultValue = StringUtils.EMPTY)
        final String file,
        @ShellOption(value = {"directory", "--directory"},
            help = "Path to the JSON/YAML service definitions directory",
            defaultValue = StringUtils.EMPTY)
        final String directory) {

        if (StringUtils.isNotBlank(file)) {
            val filePath = new File(file);
            validate(filePath);
            return;
        }
        if (StringUtils.isNotBlank(directory)) {
            val directoryPath = new File(directory);
            if (directoryPath.isDirectory()) {
                FileUtils.listFiles(directoryPath, new String[]{"json", "yml", "yaml"}, false)
                    .forEach(this::validate);
            }
        }
    }

    private void validate(final File filePath) {
        try {
            val basicFileAttributes = Files.readAttributes(filePath.toPath(), BasicFileAttributes.class);
            if (basicFileAttributes.isRegularFile() && filePath.exists()
                && filePath.canRead() && basicFileAttributes.size() > 0) {
                val validator = switch (FilenameUtils.getExtension(filePath.getPath()).toLowerCase(Locale.ENGLISH)) {
                    case "yml", "yaml" -> new RegisteredServiceYamlSerializer(applicationContext);
                    default -> new RegisteredServiceJsonSerializer(applicationContext);
                };
                val svc = Objects.requireNonNull(validator).from(filePath);
                LOGGER.info("Service [{}] is valid at [{}].", svc.getName(), filePath.getCanonicalPath());
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
