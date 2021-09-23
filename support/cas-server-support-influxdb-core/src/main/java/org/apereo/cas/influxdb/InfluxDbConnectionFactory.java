package org.apereo.cas.influxdb;

import org.apereo.cas.configuration.model.support.influxdb.InfluxDbProperties;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * This is {@link InfluxDbConnectionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class InfluxDbConnectionFactory implements AutoCloseable {
    /**
     * The Influx db.
     */
    private final InfluxDBClient influxDb;

    /**
     * The Influx db properties.
     */
    private final InfluxDbProperties influxDbProperties;


    /**
     * Instantiates a new Influx db connection factory.
     *
     * @param props the props
     */
    public InfluxDbConnectionFactory(final InfluxDbProperties props) {
        this.influxDb = InfluxDBClientFactory.create(props.getUrl(), props.getUsername(), props.getPassword().toCharArray());
        this.influxDb.enableGzip();
        this.influxDb.setLogLevel(LogLevel.NONE);
        if (LOGGER.isDebugEnabled()) {
            this.influxDb.setLogLevel(LogLevel.BODY);
        } else if (LOGGER.isInfoEnabled()) {
            this.influxDb.setLogLevel(LogLevel.BASIC);
        }
        this.influxDbProperties = props;
    }

    /**
     * Write measurement point.
     *
     * @param point the point
     */
    public void write(final Point point) {
        influxDb.getWriteApiBlocking().writePoint(influxDbProperties.getDatabase(),
            influxDbProperties.getOrganization(), point);
    }

    /**
     * Write.
     *
     * @param measurement the measurement
     * @param fields      the fields
     * @param tags        the tags
     */
    public void write(final String measurement, final Map<String, Object> fields, final Map<String, String> tags) {
        val p = Point.measurement(measurement)
            .time(Instant.now(Clock.systemUTC()), WritePrecision.NS)
            .addFields(fields)
            .addTags(tags);
        write(p);
    }

    /**
     * Delete all.
     */
    public void deleteAll() {
        val predicate = new DeletePredicateRequest();
        predicate.setStart(OffsetDateTime.now(Clock.systemUTC()).minus(10, ChronoUnit.DECADES));
        predicate.setStop(OffsetDateTime.now(Clock.systemUTC()));
        this.influxDb.getDeleteApi().delete(predicate, influxDbProperties.getDatabase(),
            influxDbProperties.getOrganization());
    }

    /**
     * Query all result.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the query result
     */
    public <T extends Serializable> List<T> query(final Class<T> clazz) {
        val query = String.format("from(bucket:\"%s\") |> range(start: 0)", influxDbProperties.getDatabase());
        return influxDb.getQueryApi().query(query, influxDbProperties.getOrganization(), clazz);
    }

    @Override
    public void close() {
        this.influxDb.close();
    }
}
