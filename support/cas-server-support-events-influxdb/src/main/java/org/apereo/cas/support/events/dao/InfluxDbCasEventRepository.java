package org.apereo.cas.support.events.dao;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.influxdb.InfluxDbConnectionFactory;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.DisposableBean;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
        influxDbConnectionFactory.write(MEASUREMENT,
            Map.of("value", event.getEventId()),
            Map.of(
                "serverIpAddress", event.getServerIpAddress(),
                "clientIpAddress", event.getClientIpAddress(),
                "principalId", event.getPrincipalId(),
                "geoLocation", Unchecked.supplier(() -> MAPPER.writeValueAsString(event.getGeoLocation())).get(),
                "creationTime", event.getCreationTime(),
                "timestamp", String.valueOf(event.getTimestamp()),
                "type", event.getType()));
        return event;
    }

    @Override
    public Collection<? extends CasEvent> load() {
        val events = new ArrayList<CasEvent>();
        val results = influxDbConnectionFactory.query(InfluxDbEvent.class);
        results.forEach(flux -> {
            val event = new CasEvent();
            val geo = Unchecked.supplier(() -> MAPPER.readValue(flux.getGeoLocation(), new TypeReference<GeoLocationRequest>() { })).get();
            event.putGeoLocation(geo);
            event.setPrincipalId(flux.getPrincipalId());
            event.setType(flux.getType());
            event.setCreationTime(flux.getCreationTime());
            event.putClientIpAddress(flux.getClientIpAddress());
            event.putServerIpAddress(flux.getServerIpAddress());
            event.putEventId(flux.getValue());
            event.putTimestamp(Long.valueOf(flux.getTimestamp()));
            events.add(event);
        });
        return events;
    }

    @Override
    public void destroy() {
        influxDbConnectionFactory.close();
    }

    @Measurement(name = MEASUREMENT)
    @Getter
    @Setter
    @ToString
    public static class InfluxDbEvent implements Serializable {
        @Column(timestamp = true)
        private Instant time;

        @Column(tag = true)
        private String principalId;

        @Column(tag = true)
        private String type;

        @Column(tag = true)
        private String clientIpAddress;

        @Column(tag = true)
        private String serverIpAddress;

        @Column(tag = true)
        private String creationTime;

        @Column(tag = true)
        private String timestamp;

        @Column(tag = true)
        private String geoLocation;
        
        @Column
        private String value;
    }
}
