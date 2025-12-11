package org.apereo.cas.shell.commands.properties;

import org.apereo.cas.metadata.CasConfigurationMetadataCatalog;
import org.apereo.cas.metadata.CasReferenceProperty;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;
import org.apereo.cas.shell.commands.CasShellCommand;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * This is {@link ExportPropertiesCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class ExportPropertiesCommand implements CasShellCommand {
    private static final int WRAP_LENGTH = 80;

    private static void writeProperty(final Writer allPropsFile, final CasReferenceProperty prop) throws Exception {
        allPropsFile.write(String.format("# Owner: %s%n", prop.getOwner()));
        allPropsFile.write(String.format("# Type: %s%n", prop.getType()));
        var description = cleanDescription(prop);
        if (StringUtils.isNotBlank(description)) {
            if (prop.isDuration()) {
                description += " This settings supports the Duration syntax.";
            }
            if (prop.isExpressionLanguage()) {
                description += " This settings supports the Spring Expression Language.";
            }
            allPropsFile.write(String.format("# Description: %s%n", description));
        }
        allPropsFile.write(String.format("# %s = %s%n", prop.getName(), prop.getDefaultValue()));
        if (StringUtils.isNotBlank(prop.getDeprecationLevel())) {
            allPropsFile.write(String.format("# Deprecation Level: %s%n", prop.getDeprecationLevel()));
        }
        if (StringUtils.isNotBlank(prop.getDeprecationReason())) {
            allPropsFile.write(String.format("# Deprecation Reason: %s%n", prop.getDeprecationReason()));
        }
        if (StringUtils.isNotBlank(prop.getDeprecationReplacement())) {
            allPropsFile.write(String.format("# Deprecation Replacement: %s%n", prop.getDeprecationReplacement()));
        }
        allPropsFile.write("#############################################\n");
    }

    private static String cleanDescription(final CasReferenceProperty property) {
        return WordUtils.wrap(property.getDescription().replace("\n", " "),
            WRAP_LENGTH, System.lineSeparator() + "# ", true);
    }

    /**
     * Export properties.
     *
     * @param dir the directory for the configuration export
     * @throws Exception the exception
     */
    @Command(group = "CAS Properties", name = "export-props", description = "Export CAS properties and settings from configuration metadata.")
    public void exportProperties(
        @Option(
            longName = "dir",
            description = "Path to a directory where reference configuration files would be exported.",
            defaultValue = "./etc/cas/config")
        final String dir) throws Exception {

        val allProps = CasConfigurationMetadataCatalog.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.ALL)
                .build());
        try (val writer = Files.newBufferedWriter(new File(dir, "all-properties.ref").toPath(), Charset.defaultCharset())) {
            allProps.properties().forEach(Unchecked.consumer(prop -> writeProperty(writer, prop)));
            writer.flush();
        }

        val casProps = CasConfigurationMetadataCatalog.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
                .build());
        try (val writer = Files.newBufferedWriter(new File(dir, "cas-properties.ref").toPath(), Charset.defaultCharset())) {
            casProps.properties().forEach(Unchecked.consumer(prop -> writeProperty(writer, prop)));
            writer.flush();
        }

        val thirdPartyProperties = CasConfigurationMetadataCatalog.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.THIRD_PARTY)
                .build());
        try (val writer = Files.newBufferedWriter(new File(dir, "thirdparty-properties.ref").toPath(), Charset.defaultCharset())) {
            thirdPartyProperties.properties().forEach(Unchecked.consumer(prop -> writeProperty(writer, prop)));
            writer.flush();
        }
        LOGGER.info("Exported configuration properties to [{}]", new File(dir).getAbsolutePath());
    }
}
