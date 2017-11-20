package org.apereo.cas.configuration.model.support.jpa;

import java.io.Serializable;

/**
 * Configuration properties class for database.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

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
    
    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(final boolean showSql) {
        this.showSql = showSql;
    }

    public boolean isGenDdl() {
        return genDdl;
    }

    public void setGenDdl(final boolean genDdl) {
        this.genDdl = genDdl;
    }
    
}
