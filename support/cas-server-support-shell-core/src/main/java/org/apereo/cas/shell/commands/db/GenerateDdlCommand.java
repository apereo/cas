package org.apereo.cas.shell.commands.db;

import org.apereo.cas.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.JdbcSettings;
import org.hibernate.cfg.SchemaToolingSettings;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This is {@link GenerateDdlCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ShellCommandGroup("Relational Databases")
@ShellComponent
@Slf4j
public class GenerateDdlCommand {
    private static final Map<String, String> DIALECTS_MAP = new TreeMap<>();

    static {
        DIALECTS_MAP.put("MYSQL", MySQLDialect.class.getName());
        DIALECTS_MAP.put("PG", PostgreSQLDialect.class.getName());
        DIALECTS_MAP.put("HSQL", HSQLDialect.class.getName());
        DIALECTS_MAP.put("ORACLE", OracleDialect.class.getName());
        DIALECTS_MAP.put("MARIADB", MariaDBDialect.class.getName());
        DIALECTS_MAP.put("SQLSERVER", SQLServerDialect.class.getName());
    }

    private static SchemaExport.Action getAction(final boolean dropSchema, final boolean createSchema) {
        if (createSchema && dropSchema) {
            return SchemaExport.Action.BOTH;
        }
        if (createSchema) {
            return SchemaExport.Action.CREATE;
        }
        if (dropSchema) {
            return SchemaExport.Action.DROP;
        }
        return SchemaExport.Action.NONE;
    }

    /**
     * Generate.
     *
     * @param file         the file
     * @param dialect      the dialect
     * @param jdbcUrl      the jdbc url
     * @param delimiter    the delimiter
     * @param pretty       the pretty
     * @param dropSchema   the drop schema
     * @param createSchema the create schema
     * @param haltOnError  the halt on error
     * @return the file
     */
    @ShellMethod(key = "generate-ddl", value = "Generate database DDL scripts")
    public String generate(
        @ShellOption(value = {"file", "--file"},
            help = "DDL file to contain to generated script",
            defaultValue = "/etc/cas/config/cas-db-schema.sql")
        final String file,
        @ShellOption(value = {"dialect", "--dialect"},
            help = "Database dialect class",
            defaultValue = "HSQL")
        final String dialect,
        @ShellOption(value = {"url", "--url"},
            help = "JDBC database connection URL",
            defaultValue = "jdbc:hsqldb:mem:cas")
        final String jdbcUrl,
        @ShellOption(value = {"delimiter", "--delimiter"},
            help = "Delimiter to use for separation of statements when generating SQL",
            defaultValue = ";")
        final String delimiter,
        @ShellOption(value = {"pretty", "--pretty"},
            help = "Format DDL scripts and pretty-print the output",
            defaultValue = "false")
        final Boolean pretty,
        @ShellOption(value = {"dropSchema", "--dropSchema"},
            help = "Generate DROP SQL statements in the DDL",
            defaultValue = "false")
        final Boolean dropSchema,
        @ShellOption(value = {"createSchema", "--createSchema"},
            help = "Generate DROP SQL statements in the DDL",
            defaultValue = "false")
        final Boolean createSchema,
        @ShellOption(value = {"haltOnError", "--haltOnError"},
            help = "Halt if an error occurs during the generation process",
            defaultValue = "false")
        final Boolean haltOnError) {

        LOGGER.info("Requested database dialect type [{}]", dialect);
        val dialectName = DIALECTS_MAP.getOrDefault(dialect.trim(), dialect);
        LOGGER.info("Using database dialect class [{}]", dialectName);
        if (!dialectName.contains(".")) {
            LOGGER.warn("Dialect name must be a fully qualified class name. Supported dialects by default are [{}] "
                        + "or you may specify the dialect class directly", DIALECTS_MAP.keySet());
            return null;
        }

        val svcRegistry = new StandardServiceRegistryBuilder();

        val settings = new HashMap<String, Object>();
        settings.put(JdbcSettings.DIALECT, dialectName);
        settings.put(JdbcSettings.URL, jdbcUrl);
        settings.put(SchemaToolingSettings.HBM2DDL_AUTO, "none");
        settings.put(JdbcSettings.SHOW_SQL, "true");
        svcRegistry.applySettings(settings);

        LOGGER.info("Collecting entity metadata sources...");
        val metadata = new MetadataSources(svcRegistry.build());
        ReflectionUtils.findClassesWithAnnotationsInPackage(
                        Set.of(MappedSuperclass.class, Entity.class),
                        "org.apereo.cas")
                .forEach(metadata::addAnnotatedClass);

        val metadataSources = metadata.buildMetadata();

        val export = new SchemaExport();
        export.setDelimiter(delimiter);
        export.setOutputFile(file);
        export.setFormat(BooleanUtils.toBoolean(pretty));
        export.setHaltOnError(BooleanUtils.toBoolean(haltOnError));
        export.setManageNamespaces(true);

        val action = getAction(BooleanUtils.toBoolean(dropSchema), BooleanUtils.toBoolean(createSchema));
        LOGGER.info("Exporting Database DDL to [{}] using dialect [{}] with export type set to [{}]", file, dialect, action);
        export.execute(EnumSet.of(TargetType.SCRIPT, TargetType.STDOUT), SchemaExport.Action.BOTH, metadataSources);
        LOGGER.info("Database DDL is exported to [{}]", file);
        return file;
    }
}
