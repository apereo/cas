package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import javax.sql.DataSource;
import java.sql.Driver;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link JdbcDriversRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class JdbcDriversRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
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

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS};
        entries.forEach(el -> {
            if (el instanceof final Class clazz) {
                hints.reflection().registerType(clazz, memberCategories);
            }
            if (el instanceof final TypeReference reference) {
                hints.reflection().registerType(reference, memberCategories);
            }
        });
    }
}
