package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties class for cas.monitor.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-monitor", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class MonitorProperties implements Serializable {
    private static final long serialVersionUID = -7047060071480971606L;

    /**
     * The free memory threshold for the memory monitor.
     * If the amount of free memory available reaches this point
     * the memory monitor will report back a warning status as a health check.
     */
    private int freeMemThreshold = 10;

    /**
     * Options for monitoring the status a nd production of TGTs.
     */
    private Tgt tgt = new Tgt();

    /**
     * Options for monitoring the status a nd production of STs.
     */
    private St st = new St();

    /**
     * Options for monitoring the Load on a production server.
     * Load averages are "system load averages" that show the running thread
     * (task) demand on the system as an average number of running plus waiting
     * threads. This measures demand, which can be greater than what the system
     * is currently processing.
     */
    private Load load = new Load();

    /**
     * Warning options that generally deal with cache-based resources, etc.
     */
    @NestedConfigurationProperty
    private MonitorWarningProperties warn = new MonitorWarningProperties();

    /**
     * Options for monitoring JDBC resources.
     */
    private Jdbc jdbc = new Jdbc();

    /**
     * Options for monitoring LDAP resources.
     */
    private List<LdapMonitorProperties> ldap = new ArrayList<>(0);

    /**
     * Options for monitoring Memcached resources.
     */
    private Memcached memcached = new Memcached();

    /**
     * Options for monitoring MongoDb resources.
     */
    private MongoDb mongo = new MongoDb();

    /**
     * Properties relevant to endpoint security, etc.
     */
    @NestedConfigurationProperty
    private ActuatorEndpointsMonitorProperties endpoints = new ActuatorEndpointsMonitorProperties();

    @RequiresModule(name = "cas-server-core-monitor", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class St implements Serializable {

        private static final long serialVersionUID = -8167395674267219982L;

        /**
         * Warning settings for this monitor.
         */
        @NestedConfigurationProperty
        private MonitorWarningProperties warn = new MonitorWarningProperties(5000);
    }

    @RequiresModule(name = "cas-server-core-monitor", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Tgt implements Serializable {

        private static final long serialVersionUID = -2756454350350278724L;

        /**
         * Warning options for monitoring TGT production.
         */
        @NestedConfigurationProperty
        private MonitorWarningProperties warn = new MonitorWarningProperties(10000);
    }

    @RequiresModule(name = "cas-server-core-monitor", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Load implements Serializable {

        private static final long serialVersionUID = 5504478373010611957L;

        /**
         * Warning settings for this monitor.
         */
        @NestedConfigurationProperty
        private MonitorWarningProperties warn = new MonitorWarningProperties(25);
    }

    @RequiresModule(name = "cas-server-support-memcached-monitor")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Memcached extends BaseMemcachedProperties {

        private static final long serialVersionUID = -9139788158851782673L;
    }

    @RequiresModule(name = "cas-server-support-mongo-monitor")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class MongoDb extends BaseMongoDbProperties {

        private static final long serialVersionUID = -1918436901491275547L;
    }

    @RequiresModule(name = "cas-server-support-jdbc-monitor")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Jdbc extends AbstractJpaProperties {

        private static final long serialVersionUID = -7139788158851782673L;

        /**
         * The query to execute against the database to monitor status.
         */
        private String validationQuery = "SELECT 1";

        /**
         * When monitoring the JDBC connection pool, indicates the amount of time the operation must wait
         * before it times outs and considers the pool in bad shape.
         */
        private String maxWait = "PT5S";
    }

}
