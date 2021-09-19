package org.apereo.cas.influxdb;

import org.apereo.cas.configuration.model.core.events.InfluxDbEventsProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InfluxDbConnectionFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("InfluxDb")
@EnabledIfPortOpen(port = 8086)
public class InfluxDbConnectionFactoryTests {
    private InfluxDbConnectionFactory factory;

    @BeforeEach
    public void init() {
        val props = new InfluxDbEventsProperties()
            .setDatabase("casEventsDatabase")
            .setOrganization("CAS")
            .setPassword("password")
            .setUsername("root")
            .setUrl("http://localhost:8086");
        factory = new InfluxDbConnectionFactory(props);
        factory.deleteAll();
    }

    @AfterEach
    public void shutdown() {
        factory.deleteAll();
        factory.close();
    }

    @Test
    public void verifyWritePoint() {
        factory.deleteAll();
        factory.write("events", Map.of("value", 1234.5678), Map.of("hostname", "cas.example.org"));
        val result = factory.query(InfluxEvent.class);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("cas.example.org", result.get(0).getHostname());
        assertEquals(1234.5678, result.get(0).getValue());
    }

    @Measurement(name = "events")
    @Getter
    @Setter
    @ToString
    public static class InfluxEvent implements Serializable {
        @Column(name = "time", timestamp = true)
        private Instant time;

        @Column(name = "hostname", tag = true)
        private String hostname;

        @Column(name = "value")
        private Double value;
    }
}
