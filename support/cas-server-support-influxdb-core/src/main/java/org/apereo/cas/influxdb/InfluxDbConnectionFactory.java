package org.apereo.cas.influxdb;

import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.influxdb.InfluxDbProperties;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link InfluxDbConnectionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InfluxDbConnectionFactory implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDbConnectionFactory.class);

    /**
     * The Influx db.
     */
    private final InfluxDB influxDb;

    /**
     * The Influx db properties.
     */
    private InfluxDbProperties influxDbProperties;

    /**
     * Instantiates a new Influx db connection factory.
     *
     * @param url          the url
     * @param uid          the uid
     * @param psw          the psw
     * @param dbName       the db name
     * @param dropDatabase the drop database
     */
    public InfluxDbConnectionFactory(final String url, final String uid,
                                     final String psw, final String dbName,
                                     final boolean dropDatabase) {

        if (StringUtils.isBlank(dbName) || StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("Database name/url cannot be blank and must be specified");
        }

        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        this.influxDb = InfluxDBFactory.connect(url, uid, psw, builder);
        this.influxDb.enableGzip();

        if (dropDatabase) {
            this.influxDb.deleteDatabase(dbName);
        }

        if (!this.influxDb.databaseExists(dbName)) {
            this.influxDb.createDatabase(dbName);
        }

        this.influxDb.setLogLevel(InfluxDB.LogLevel.NONE);
        if (LOGGER.isDebugEnabled()) {
            this.influxDb.setLogLevel(InfluxDB.LogLevel.FULL);
        } else if (LOGGER.isInfoEnabled()) {
            this.influxDb.setLogLevel(InfluxDB.LogLevel.BASIC);
        } else if (LOGGER.isWarnEnabled()) {
            this.influxDb.setLogLevel(InfluxDB.LogLevel.HEADERS);
        } else if (LOGGER.isErrorEnabled()) {
            this.influxDb.setLogLevel(InfluxDB.LogLevel.NONE);
        }
    }

    /**
     * Instantiates a new Influx db connection factory.
     *
     * @param props the props
     */
    public InfluxDbConnectionFactory(final InfluxDbProperties props) {
        this(props.getUrl(), props.getUsername(), props.getPassword(), props.getDatabase(), props.isDropDatabase());

        if (StringUtils.isNotBlank(props.getRetentionPolicy())) {
            this.influxDb.setRetentionPolicy(props.getRetentionPolicy());
        }

        influxDb.setConsistency(InfluxDB.ConsistencyLevel.valueOf(props.getConsistencyLevel().toUpperCase()));

        if (props.getPointsToFlush() > 0 && props.getBatchInterval() > 0) {
            this.influxDb.enableBatch(props.getPointsToFlush(), props.getBatchInterval(), TimeUnit.MILLISECONDS);
        }

        this.influxDbProperties = props;
    }

    /**
     * Write measurement point.
     *
     * @param point the point
     */
    public void write(final Point point) {
        this.influxDb.write(influxDbProperties.getDatabase(), influxDbProperties.getRetentionPolicy(), point);
    }

    /**
     * Write measurement point.
     *
     * @param point  the point
     * @param dbName the db name
     */
    public void write(final Point point, final String dbName) {
        this.influxDb.write(dbName, "autogen", point);
    }

    /**
     * Query all result.
     *
     * @param measurement the measurement
     * @return the query result
     */
    public QueryResult query(final String measurement) {
        return query("*", measurement);
    }

    /**
     * Query result.
     *
     * @param fields      the fields
     * @param measurement the measurement
     * @return the query result
     */
    public QueryResult query(final String fields, final String measurement) {
        return query(fields, measurement, influxDbProperties.getDatabase());
    }

    /**
     * Query result.
     *
     * @param fields      the fields
     * @param measurement the table
     * @param dbName      the db name
     * @return the query result
     */
    public QueryResult query(final String fields, final String measurement, final String dbName) {
        final String filter = String.format("SELECT %s FROM %s", fields, measurement);
        final Query query = new Query(filter, dbName);
        return this.influxDb.query(query);
    }

    @Override
    public void close() {
        this.influxDb.close();
    }
}
