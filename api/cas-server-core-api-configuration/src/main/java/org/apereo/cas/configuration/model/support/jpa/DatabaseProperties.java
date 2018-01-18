package org.apereo.cas.configuration.model.support.jpa;

import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link DatabaseProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
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
}
