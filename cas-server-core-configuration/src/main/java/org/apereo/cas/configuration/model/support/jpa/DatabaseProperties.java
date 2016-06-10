package org.apereo.cas.configuration.model.support.jpa;

/**
 * Configuration properties class for database.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class DatabaseProperties {

    private boolean showSql = true;

    private boolean genDdl = true;

    private Cleaner cleaner = new Cleaner();

    public Cleaner getCleaner() {
        return cleaner;
    }

    public void setCleaner(final Cleaner cleaner) {
        this.cleaner = cleaner;
    }

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

    public static class Cleaner {
        private String appid = "cas-ticket-registry-cleaner";

        public String getAppid() {
            return appid;
        }

        public void setAppid(final String appid) {
            this.appid = appid;
        }
    }
}
