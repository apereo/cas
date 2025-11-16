package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import javax.sql.DataSource;
import java.sql.Driver;
import java.util.List;

/**
 * This is {@link JdbcDriversRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class JdbcDriversRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, findSubclassesInPackage(Driver.class,
            "com.mysql", "net.sourceforge", "org.h2",
            "org.mariadb", "org.postgresql", "org.apache.ignite",
            "org.sqlite", "org.hsqldb", "oracle.jdbc", "com.microsoft"));

        registerReflectionHints(hints, List.of(
            TypeReference.of("oracle.jdbc.logging.annotations.Feature"),
            TypeReference.of("org.hsqldb.dbinfo.DatabaseInformationFull"),
            TypeReference.of("org.hsqldb.dbinfo.DatabaseInformation")

        ));
        hints.resources()
            .registerResourceBundle("org/hsqldb/resources/sql-state-messages")
            .registerPattern("org/hsqldb/resources/*.sql")
            .registerPattern("org/hsqldb/resources/*.properties");

        registerReflectionHints(hints, List.of(DataSource.class));
    }
}
