package org.apereo.cas.support.events.dao;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.influxdb.InfluxDbConnectionFactory;
import org.apereo.cas.support.events.CasEventAggregate;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.influxdb.v3.client.PointValues;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.DisposableBean;
import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This is {@link InfluxDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class InfluxDbCasEventRepository extends AbstractCasEventRepository implements DisposableBean {
    private static final String MEASUREMENT = "InfluxDbCasEventRepositoryCasEvents";

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(true).build().toObjectMapper();

    private final InfluxDbConnectionFactory influxDbConnectionFactory;

    public InfluxDbCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter,
                                      final InfluxDbConnectionFactory influxDbConnectionFactory) {
        super(eventRepositoryFilter);
        this.influxDbConnectionFactory = influxDbConnectionFactory;
    }

    @Override
    public CasEvent saveInternal(final CasEvent event) {
        event.assignIdIfNecessary();
        val tags = Map.of(
            "serverIpAddress", Objects.requireNonNull(event.getServerIpAddress()),
            "clientIpAddress", Objects.requireNonNull(event.getClientIpAddress()),
            "principalId", event.getPrincipalId(),
            "geoLocation", MAPPER.writeValueAsString(event.getGeoLocation()),
            "creationTime", String.valueOf(event.getCreationTime().toEpochMilli()),
            "tenant", StringUtils.defaultIfBlank(event.getTenant(), "CAS"),
            "timestamp", String.valueOf(event.getTimestamp()),
            "type", event.getType());
        influxDbConnectionFactory.write(MEASUREMENT, Map.of("value", event.getEventId()), tags);
        return event;
    }

    @Override
    public Stream<? extends CasEvent> load() {
        val results = influxDbConnectionFactory.query(MEASUREMENT);
        return results.map(InfluxDbCasEventRepository::extractCasEventFromPointValues);
    }

    private static CasEvent extractCasEventFromPointValues(final PointValues pointValues) {
        val event = new CasEvent().assignIdIfNecessary();

        val geoLocation = pointValues.getTag("geoLocation");
        val geo = MAPPER.readValue(geoLocation, GeoLocationRequest.class);
        event.putGeoLocation(geo);

        event.setPrincipalId(Objects.requireNonNull(pointValues.getTag("principalId")));
        event.setType(Objects.requireNonNull(pointValues.getTag("type")));
        event.setCreationTime(Instant.ofEpochMilli(Long.parseLong(Objects.requireNonNull(pointValues.getTag("creationTime")))));
        event.putClientIpAddress(pointValues.getTag("clientIpAddress"));
        event.putServerIpAddress(pointValues.getTag("serverIpAddress"));
        event.putEventId(pointValues.getStringField("value"));
        event.putTimestamp(Long.valueOf(Objects.requireNonNull(pointValues.getTag("timestamp"))));
        event.putTenant(pointValues.getTag("tenant"));
        return event;
    }

    @Override
    public void destroy() {
        influxDbConnectionFactory.close();
    }

    @Override
    public Stream<CasEventAggregate> aggregate(@Nullable final Class type, final Duration start) {
        val initialSql = """
            SELECT
                DATE_BIN(INTERVAL '1 hour', time) AS window,
                "type",
                "tenant",
                COUNT("value") AS count
              FROM "${measurement}"
              WHERE ${where}
              GROUP BY window, "type", "tenant"
              ORDER BY window, "type", "tenant";
            """
            .stripLeading()
            .stripTrailing();

        var whereClause = "time >= NOW() - INTERVAL '%s hours'".formatted(start.toHours());
        if (type != null) {
            whereClause += " AND \"type\" = '%s'".formatted(type.getName());
        }
        val sub = new StringSubstitutor(Map.of(
            "measurement", MEASUREMENT,
            "where", whereClause
        ));
        val sql = sub.replace(initialSql);
        LOGGER.debug("Executing SQL query [{}]", sql);
        val rows = influxDbConnectionFactory.query(MEASUREMENT, sql);
        return rows
            .map(row -> new CasEventAggregate(
                row.getField("window", LocalDateTime.class),
                row.getTag("type"),
                row.getIntegerField("count"),
                row.getTag("tenant")
            ))
            .onClose(rows::close);
    }
}
