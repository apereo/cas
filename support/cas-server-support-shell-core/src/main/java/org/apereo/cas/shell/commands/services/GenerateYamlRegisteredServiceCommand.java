package org.apereo.cas.shell.commands.services;

import module java.base;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.shell.commands.CasShellCommand;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

/**
 * This is {@link GenerateYamlRegisteredServiceCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GenerateYamlRegisteredServiceCommand implements CasShellCommand {
    private static final int SEP_LINE_LENGTH = 70;

    @Autowired
    private ConfigurableApplicationContext applicationContext;


    /**
     * Validate service.
     *
     * @param file        the file
     * @param destination the destination
     * @return the file
     */
    @Command(group = "Registered Services", name = "generate-yaml", description = "Generate a YAML registered service definition")
    public File generateYaml(
        @Option(
            longName = "file",
            description = "Path to the JSON service definition file"
        )
        final String file,

        @Option(
            longName = "destination",
            description = "Path to the destination YAML service definition file"
        )
        final String destination) {
        val filePath = new File(file);
        val result = StringUtils.isBlank(destination) ? null : new File(destination);
        generate(filePath, result);
        return filePath;
    }

    private void generate(final File filePath, final File result) {
        try {
            val validator = new RegisteredServiceJsonSerializer(applicationContext);
            val basicFileAttributes = Files.readAttributes(filePath.toPath(), BasicFileAttributes.class);
            if (basicFileAttributes.isRegularFile() && filePath.exists()
                && filePath.canRead() && basicFileAttributes.size() > 0) {
                val svc = validator.from(filePath);
                LOGGER.info("Service [{}] is valid at [{}].", svc.getName(), filePath.getCanonicalPath());
                val yaml = new RegisteredServiceYamlSerializer(applicationContext);
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
