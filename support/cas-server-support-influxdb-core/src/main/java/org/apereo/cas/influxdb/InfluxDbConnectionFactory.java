package org.apereo.cas.influxdb;

import org.apereo.cas.configuration.model.support.influxdb.InfluxDbProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import com.influxdb.v3.client.PointValues;
import com.influxdb.v3.client.config.ClientConfig;
import com.influxdb.v3.client.query.QueryOptions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This is {@link InfluxDbConnectionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class InfluxDbConnectionFactory implements AutoCloseable {
    private final InfluxDBClient influxDb;

    private final ClientConfig clientConfig;
    
    public InfluxDbConnectionFactory(final InfluxDbProperties props) {
        try {
            val definedToken = SpringExpressionLanguageValueResolver.getInstance().resolve(props.getToken());
            val tokenFile = FileUtils.getFile(definedToken);
            val token = tokenFile.exists()
                ? FileUtils.readFileToString(tokenFile, StandardCharsets.UTF_8).stripTrailing()
                : definedToken;
            clientConfig = new ClientConfig.Builder()
                .token(token.toCharArray())
                .database(props.getDatabase())
                .host(props.getUrl())
                .organization(props.getOrganization())
                .build();
            influxDb = InfluxDBClient.getInstance(clientConfig);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Write measurement point.
     *
     * @param point the point
     */
    public void write(final Point point) {
        influxDb.writePoint(point);
    }

    /**
     * Write.
     *
     * @param measurement the measurement
     * @param fields      the fields
     * @param tags        the tags
     */
    public void write(final String measurement, final Map<String, Object> fields, final Map<String, String> tags) {
        val point = Point.measurement(measurement).setTimestamp(Instant.now(Clock.systemUTC())).setTags(tags);
        fields.forEach((key, value) -> {
            val stringValue = String.valueOf(value);
            if (NumberUtils.isParsable(stringValue)) {
                point.setField(key, Double.parseDouble(stringValue));
            } else if (BooleanUtils.toBoolean(stringValue)) {
                point.setField(key, Boolean.parseBoolean(stringValue));
            } else {
                point.setField(key, stringValue);
            }
        });
        write(point);
    }


    /**
     * Query all result.
     *
     * @return the query result
     */
    public Stream<PointValues> query(final String measurement) {
        val query = "SELECT * FROM \"%s\"".formatted(measurement);
        return influxDb.queryPoints(query, new QueryOptions(Objects.requireNonNull(clientConfig.getDatabase())));
    }

    @Override
    public void close() {
        FunctionUtils.doAndHandle(__ -> influxDb.close());
    }
}
