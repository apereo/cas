package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.ConnectionPoolingProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for cas.monitor.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class MonitorProperties {

    private int freeMemThreshold = 10;

    private Tgt tgt = new Tgt();
    
    private St st = new St();

    private Warn warn = new Warn();

    private Jdbc jdbc = new Jdbc();
    
    private Ldap ldap = new Ldap();
    
    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(final Jdbc jdbc) {
        this.jdbc = jdbc;
    }
    
    public Warn getWarn() {
        return warn;
    }

    public void setWarn(final Warn warn) {
        this.warn = warn;
    }

    public int getFreeMemThreshold() {
        return freeMemThreshold;
    }

    public void setFreeMemThreshold(final int freeMemThreshold) {
        this.freeMemThreshold = freeMemThreshold;
    }

    public Tgt getTgt() {
        return tgt;
    }

    public void setTgt(final Tgt tgt) {
        this.tgt = tgt;
    }

    public St getSt() {
        return st;
    }

    public void setSt(final St st) {
        this.st = st;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public static class St {
        @NestedConfigurationProperty
        private Warn warn = new Warn(5000);

        public Warn getWarn() {
            return warn;
        }

        public void setWarn(final Warn warn) {
            this.warn = warn;
        }
    }

    public static class Tgt {
        @NestedConfigurationProperty
        private Warn warn = new Warn(10000);

        public Warn getWarn() {
            return warn;
        }

        public void setWarn(final Warn warn) {
            this.warn = warn;
        }
    }

    public static class Warn {
        private int threshold = 10;
        private long evictionThreshold;


        public Warn() {
        }

        public Warn(final int threshold) {
            this.threshold = threshold;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(final int threshold) {
            this.threshold = threshold;
        }

        public long getEvictionThreshold() {
            return evictionThreshold;
        }

        public void setEvictionThreshold(final long evictionThreshold) {
            this.evictionThreshold = evictionThreshold;
        }
    }

    public static class Ldap extends AbstractLdapProperties {
        private int maxWait = 5000;
        
        @NestedConfigurationProperty
        private ConnectionPoolingProperties pool = new ConnectionPoolingProperties();

        public ConnectionPoolingProperties getPool() {
            return pool;
        }

        public void setPool(final ConnectionPoolingProperties pool) {
            this.pool = pool;
        }

        public int getMaxWait() {
            return maxWait;
        }

        public void setMaxWait(final int maxWait) {
            this.maxWait = maxWait;
        }
    }
    
    public static class Jdbc extends AbstractJpaProperties {
        private String validationQuery = "SELECT 1";
        private int maxWait = 5000;
        
        public String getValidationQuery() {
            return validationQuery;
        }

        public void setValidationQuery(final String validationQuery) {
            this.validationQuery = validationQuery;
        }

        public int getMaxWait() {
            return maxWait;
        }

        public void setMaxWait(final int maxWait) {
            this.maxWait = maxWait;
        }

    }
}
