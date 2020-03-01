package org.apereo.cas.shell.commands.db;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MariaDB102Dialect;
import org.hibernate.dialect.MariaDB103Dialect;
import org.hibernate.dialect.MariaDB10Dialect;
import org.hibernate.dialect.MariaDB53Dialect;
import org.hibernate.dialect.MariaDBDialect;
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
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
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

        DIALECTS_MAP.put("MARIADB", MariaDBDialect.class.getName());
        DIALECTS_MAP.put("MARIADB53", MariaDB53Dialect.class.getName());
        DIALECTS_MAP.put("MARIADB10", MariaDB10Dialect.class.getName());
        DIALECTS_MAP.put("MARIADB102", MariaDB102Dialect.class.getName());
        DIALECTS_MAP.put("MARIADB103", MariaDB103Dialect.class.getName());

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
            defaultValue = "/etc/cas/config/cas-db-schema.sql") final String file,
        @ShellOption(value = {"dialect", "--dialect"},
            help = "Database dialect class",
            defaultValue = "HSQL") final String dialect,
        @ShellOption(value = {"url", "--url"},
            help = "JDBC database connection URL",
            defaultValue = "jdbc:hsqldb:mem:cas") final String jdbcUrl,
        @ShellOption(value = {"delimiter", "--delimiter"},
            help = "Delimiter to use for separation of statements when generating SQL",
            defaultValue = ";") final String delimiter,
        @ShellOption(value = {"pretty", "--pretty"},
            help = "Format DDL scripts and pretty-print the output",
            defaultValue = "true") final boolean pretty,
        @ShellOption(value = {"dropSchema", "--dropSchema"},
            help = "Generate DROP SQL statements in the DDL",
            defaultValue = "true") final boolean dropSchema,
        @ShellOption(value = {"createSchema", "--createSchema"},
            help = "Generate DROP SQL statements in the DDL",
            defaultValue = "true") final boolean createSchema,
        @ShellOption(value = {"haltOnError", "--haltOnError"},
            help = "Halt if an error occurs during the generation process",
            defaultValue = "true") final boolean haltOnError) {

        LOGGER.info("Requested database dialect type [{}]", dialect);
        val dialectName = DIALECTS_MAP.getOrDefault(dialect.trim(), dialect);
        LOGGER.info("Using database dialect class [{}]", dialectName);
        if (!dialectName.contains(".")) {
            LOGGER.warn("Dialect name must be a fully qualified class name. Supported dialects by default are [{}] "
                + "or you may specify the dialect class directly", DIALECTS_MAP.keySet());
            return null;
        }

        val svcRegistry = new StandardServiceRegistryBuilder();

        val settings = new HashMap<String, String>();
        settings.put(AvailableSettings.DIALECT, dialectName);
        settings.put(AvailableSettings.URL, jdbcUrl);
        settings.put(AvailableSettings.HBM2DDL_AUTO, "none");
        settings.put(AvailableSettings.SHOW_SQL, "true");
        svcRegistry.applySettings(settings);

        LOGGER.info("Collecting entity metadata sources...");
        val metadata = new MetadataSources(svcRegistry.build());
        REFLECTIONS.getTypesAnnotatedWith(MappedSuperclass.class).forEach(metadata::addAnnotatedClass);
        REFLECTIONS.getTypesAnnotatedWith(Entity.class).forEach(metadata::addAnnotatedClass);
        val metadataSources = metadata.buildMetadata();

        val export = new SchemaExport();
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
        export.execute(EnumSet.of(TargetType.SCRIPT, TargetType.STDOUT), SchemaExport.Action.BOTH, metadataSources);
        LOGGER.info("Database DDL is exported to [{}]", file);
        return file;
    }
}
