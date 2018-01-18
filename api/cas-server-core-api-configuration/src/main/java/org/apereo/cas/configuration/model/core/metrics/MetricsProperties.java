package org.apereo.cas.configuration.model.core.metrics;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.influxdb.InfluxDbProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link MetricsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Slf4j
@Getter
@Setter
public class MetricsProperties implements Serializable {

    private static final long serialVersionUID = 345002357523418414L;

    /**
     * String representation of refresh interval for metrics collection.
     */
    private String refreshInterval = "PT30S";

    /**
     * Log destination name of the logging system in use for metrics output.
     */
    private String loggerName = "perfStatsLogger";

    /**
     * Export metrics to a redis database.
     */
    private Redis redis = new Redis();

    /**
     * Export metrics to a statsd database.
     */
    private Statsd statsd = new Statsd();

    /**
     * Export metrics to an mongodb database.
     */
    private MongoDb mongo = new MongoDb();

    /**
     * Export metrics to an influxdb database.
     */
    private InfluxDb influxDb = new InfluxDb();

    /**
     * Export metrics to an open tsdb database.
     */
    private OpenTsdb openTsdb = new OpenTsdb();

    @RequiresModule(name = "cas-server-support-metrics")
    @Getter
    @Setter
    public static class MongoDb extends SingleCollectionMongoDbProperties {

        private static final long serialVersionUID = 8131713495513399930L;
    }

    @RequiresModule(name = "cas-server-support-metrics")
    @Getter
    @Setter
    public static class InfluxDb extends InfluxDbProperties {

        private static final long serialVersionUID = 1231713495513399930L;
    }

    @RequiresModule(name = "cas-server-support-metrics")
    @Getter
    @Setter
    public static class Statsd implements Serializable {

        private static final long serialVersionUID = 6541713495513399930L;

        /**
         * Statsd host.
         */
        @RequiredProperty
        private String host;

        /**
         * Statd port.
         */
        @RequiredProperty
        private int port = 8125;

        /**
         * Define a prefix for statd metrics.
         */
        private String prefix = "cas";
    }

    @RequiresModule(name = "cas-server-support-metrics")
    @Getter
    @Setter
    public static class OpenTsdb implements Serializable {

        private static final long serialVersionUID = 7419713490013390030L;

        /**
         * Connection timeout.
         */
        private int connectTimeout = 10_000;

        /**
         * Reading input timeout.
         */
        private int readTimeout = 30_000;

        /**
         * Url of the Open TSDB server.
         * Typically, this is {@code http://localhost:4242/api/put}.
         */
        @RequiredProperty
        private String url;
    }

    @RequiresModule(name = "cas-server-support-metrics")
    @Getter
    @Setter
    public static class Redis extends BaseRedisProperties {

        private static final long serialVersionUID = 6419713490013390030L;

        /**
         * It is best to use a prefix that is unique to the application instance (e.g. using a random value and maybe the
         * logical name of the application to make it possible to correlate with other instances of the same application)
         */
        @RequiredProperty
        private String prefix;

        /**
         * The key is used to keep a global index of all metric names, so it should be unique globally,
         * whatever that means for your system
         * (e.g. two instances of the same system could share a Redis cache if they have distinct keys).
         */
        @RequiredProperty
        private String key;
    }
}
