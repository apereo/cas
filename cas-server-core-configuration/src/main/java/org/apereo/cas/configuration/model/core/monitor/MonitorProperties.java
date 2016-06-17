package org.apereo.cas.configuration.model.core.monitor;

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

    private DataSource dataSource = new DataSource();

    private int maxWait = 5000;

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(final int maxWait) {
        this.maxWait = maxWait;
    }


    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
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

    public static class DataSource {
        private String validationQuery = "SELECT 1";

        public String getValidationQuery() {
            return validationQuery;
        }

        public void setValidationQuery(final String validationQuery) {
            this.validationQuery = validationQuery;
        }
    }


}
