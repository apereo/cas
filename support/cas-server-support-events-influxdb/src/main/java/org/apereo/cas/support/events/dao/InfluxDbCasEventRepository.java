package org.apereo.cas.support.events.dao;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.influxdb.InfluxDbConnectionFactory;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.DisposableBean;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This is {@link InfluxDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InfluxDbCasEventRepository extends AbstractCasEventRepository implements DisposableBean {
    private static final String MEASUREMENT = "InfluxDbCasEventRepositoryCasEvents";

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final InfluxDbConnectionFactory influxDbConnectionFactory;

    public InfluxDbCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter,
                                      final InfluxDbConnectionFactory influxDbConnectionFactory) {
        super(eventRepositoryFilter);
        this.influxDbConnectionFactory = influxDbConnectionFactory;
    }

    @Override
    public CasEvent saveInternal(final CasEvent event) {
        event.assignIdIfNecessary();
        influxDbConnectionFactory.write(MEASUREMENT,
            Map.of("value", event.getEventId()),
            Map.of(
                "serverIpAddress", event.getServerIpAddress(),
                "clientIpAddress", event.getClientIpAddress(),
                "principalId", event.getPrincipalId(),
                "geoLocation", Unchecked.supplier(() -> MAPPER.writeValueAsString(event.getGeoLocation())).get(),
                "creationTime", event.getCreationTime(),
                "tenant", StringUtils.defaultString(event.getTenant()),
                "timestamp", String.valueOf(event.getTimestamp()),
                "type", event.getType()));
        return event;
    }

    @Override
    public Stream<? extends CasEvent> load() {
        val results = influxDbConnectionFactory.query(MEASUREMENT);
        return results
            .map(pointValues -> {
                val event = new CasEvent();
                val geo = Unchecked.supplier(() -> MAPPER.readValue(pointValues.getTag("geoLocation"),
                        new TypeReference<GeoLocationRequest>() {
                        }))
                    .get();
                event.putGeoLocation(geo);
                event.setPrincipalId(pointValues.getTag("principalId"));
                event.setType(pointValues.getTag("type"));
                event.setCreationTime(pointValues.getTag("creationTime"));
                event.putClientIpAddress(pointValues.getTag("clientIpAddress"));
                event.putServerIpAddress(pointValues.getTag("serverIpAddress"));
                event.putEventId(pointValues.getStringField("value"));
                event.putTimestamp(Long.valueOf(Objects.requireNonNull(pointValues.getTag("timestamp"))));
                event.putTenant(pointValues.getTag("tenant"));
                return event;
            });
    }

    @Override
    public void destroy() {
        influxDbConnectionFactory.close();
    }

}
