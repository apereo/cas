package org.apereo.cas.shell.commands;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.Oracle12cDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.dialect.PostgreSQL91Dialect;
import org.hibernate.dialect.PostgreSQL92Dialect;
import org.hibernate.dialect.PostgreSQL93Dialect;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.dialect.SQLServer2005Dialect;
import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.reflections.Reflections;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link GenerateDdlCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Service
@Slf4j
public class GenerateDdlCommand implements CommandMarker {


    private static final Map<String, String> DIALECTS_MAP = new TreeMap<>();
    private static final Reflections REFLECTIONS = new Reflections("org.apereo.cas");

    static {
        DIALECTS_MAP.put("MYSQL", MySQLDialect.class.getName());
        DIALECTS_MAP.put("MYSQL57", MySQL57Dialect.class.getName());
        DIALECTS_MAP.put("MYSQL55", MySQL57Dialect.class.getName());
        DIALECTS_MAP.put("MYSQL5", MySQL5Dialect.class.getName());

        DIALECTS_MAP.put("PG95", PostgreSQL95Dialect.class.getName());
        DIALECTS_MAP.put("PG94", PostgreSQL94Dialect.class.getName());
        DIALECTS_MAP.put("PG93", PostgreSQL93Dialect.class.getName());
        DIALECTS_MAP.put("PG92", PostgreSQL92Dialect.class.getName());
        DIALECTS_MAP.put("PG91", PostgreSQL91Dialect.class.getName());

        DIALECTS_MAP.put("HSQL", HSQLDialect.class.getName());

        DIALECTS_MAP.put("ORACLE8i", Oracle8iDialect.class.getName());
        DIALECTS_MAP.put("ORACLE9i", Oracle9iDialect.class.getName());
        DIALECTS_MAP.put("ORACLE10g", Oracle10gDialect.class.getName());
        DIALECTS_MAP.put("ORACLE12c", Oracle12cDialect.class.getName());

        DIALECTS_MAP.put("SQLSERVER", SQLServerDialect.class.getName());
        DIALECTS_MAP.put("SQLSERVER2005", SQLServer2005Dialect.class.getName());
        DIALECTS_MAP.put("SQLSERVER2008", SQLServer2008Dialect.class.getName());
        DIALECTS_MAP.put("SQLSERVER2012", SQLServer2012Dialect.class.getName());
    }

    /**
     * Generate.
     *
     * @param file         the file
     * @param dialect      the dialect
     * @param delimiter    the delimiter
     * @param pretty       the pretty
     * @param dropSchema   the drop schema
     * @param createSchema the create schema
     * @param haltOnError  the halt on error
     */
    @CliCommand(value = "generate-ddl", help = "Generate database DDL scripts")
    public void generate(
        @CliOption(key = {"file"},
            help = "DDL file to contain to generated script",
            specifiedDefaultValue = "/etc/cas/config/cas-db-schema.sql",
            unspecifiedDefaultValue = "/etc/cas/config/cas-db-schema.sql",
            optionContext = "DDL file to contain to generated script") final String file,
        @CliOption(key = {"dialect"},
            help = "Database dialect class",
            specifiedDefaultValue = "HSQL",
            unspecifiedDefaultValue = "HSQL",
            optionContext = "Database dialect class") final String dialect,
        @CliOption(key = {"delimiter"},
            help = "Delimiter to use for separation of statements when generating SQL",
            specifiedDefaultValue = ";",
            unspecifiedDefaultValue = ";",
            optionContext = "Delimiter to use for separation of statements when generating SQL") final String delimiter,
        @CliOption(key = {"pretty"},
            help = "Format DDL scripts and pretty-print the output",
            specifiedDefaultValue = "true",
            unspecifiedDefaultValue = "true",
            optionContext = "Format DDL scripts and pretty-print the output") final boolean pretty,
        @CliOption(key = {"dropSchema"},
            help = "Generate DROP SQL statements in the DDL",
            specifiedDefaultValue = "true",
            unspecifiedDefaultValue = "true",
            optionContext = "Generate DROP statements in the DDL") final boolean dropSchema,
        @CliOption(key = {"createSchema"},
            help = "Generate DROP SQL statements in the DDL",
            specifiedDefaultValue = "true",
            unspecifiedDefaultValue = "true",
            optionContext = "Generate CREATE SQL statements in the DDL") final boolean createSchema,
        @CliOption(key = {"haltOnError"},
            help = "Halt if an error occurs during the generation process",
            specifiedDefaultValue = "true",
            unspecifiedDefaultValue = "true",
            optionContext = "Halt if an error occurs during the generation process") final boolean haltOnError) {

        final String dialectName = DIALECTS_MAP.getOrDefault(dialect.trim().toUpperCase(), dialect);
        LOGGER.info("Using database dialect class [{}]", dialectName);
        if (!dialectName.contains(".")) {
            LOGGER.warn("Dialect name must be a fully qualified class name. Supported dialects by default are [{}] "
                + "or you may specify the dialect class directly", DIALECTS_MAP.keySet());
            return;
        }

        final StandardServiceRegistryBuilder svcRegistry = new StandardServiceRegistryBuilder();
        if (StringUtils.isNotBlank(dialectName)) {
            svcRegistry.applySetting(AvailableSettings.DIALECT, dialect);
        }
        final MetadataSources metadata = new MetadataSources(svcRegistry.build());
        REFLECTIONS.getTypesAnnotatedWith(MappedSuperclass.class).forEach(metadata::addAnnotatedClass);
        REFLECTIONS.getTypesAnnotatedWith(Entity.class).forEach(metadata::addAnnotatedClass);
        final SchemaExport export = new SchemaExport();
        export.setDelimiter(delimiter);
        export.setOutputFile(file);
        export.setFormat(pretty);
        export.setHaltOnError(haltOnError);
        export.setManageNamespaces(true);
        
        final SchemaExport.Action action;
        if (createSchema && dropSchema) {
            action = SchemaExport.Action.BOTH;
        } else if (createSchema) {
            action = SchemaExport.Action.CREATE;
        } else if (dropSchema) {
            action = SchemaExport.Action.DROP;
        } else {
            action = SchemaExport.Action.NONE;
        }
        LOGGER.info("Exporting Database DDL to [{}] using dialect [{}] with export type set to [{}]", file, dialect, action);
        export.execute(EnumSet.of(TargetType.SCRIPT, TargetType.STDOUT), SchemaExport.Action.BOTH, metadata.buildMetadata());
        LOGGER.info("Database DDL is exported to [{}]", file);
    }
}
