package org.apereo.cas.configuration.model.support.jpa;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for database.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "database", ignoreUnknownFields = false)
public class DatabaseProperties {

    private boolean showSql = true;

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
