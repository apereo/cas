package org.apereo.cas.configuration.model.support.jpa;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link DatabaseProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-jdbc-drivers")
@Accessors(chain = true)
public class DatabaseProperties implements Serializable {

    private static final long serialVersionUID = 7740236971148591965L;

    /**
     * Whether SQL queries should be displayed in the console/logs.
     */
    private boolean showSql;

    /**
     * Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
     */
    private boolean genDdl = true;

    /**
     * When choosing physical table names, determine whether names
     * should be considered case-insensitive.
     */
    private boolean caseInsensitive;

    /**
     * Indicate a physical table name
     * to be used by the hibernate naming strategy
     * in case table names need to be customized for the
     * specific type of database. The key here indicates
     * the CAS-provided table name and the value is the
     * translate physical name for the database. If a match
     * is not found for the CAS-provided table name, then that
     * name will be used by default.
     */
    private Map<String, String> physicalTableNames = new LinkedCaseInsensitiveMap<>();
}
