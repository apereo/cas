package org.apereo.cas.influxdb;

import org.apereo.cas.configuration.model.core.events.InfluxDbEventsProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

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

import java.io.Serial;
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
@EnabledIfListeningOnPort(port = 8086)
class InfluxDbConnectionFactoryTests {
    private InfluxDbConnectionFactory factory;

    @BeforeEach
    void init() {
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
    void verifyWritePoint() {
        factory.deleteAll();
        factory.write("events", Map.of("value", 1234.5678), Map.of("hostname", "cas.example.org"));
        val result = factory.query(InfluxEvent.class);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("cas.example.org", result.getFirst().getHostname());
        assertEquals(1234.5678, result.getFirst().getValue());
    }

    @Measurement(name = "events")
    @Getter
    @Setter
    @ToString
    public static class InfluxEvent implements Serializable {
        @Serial
        private static final long serialVersionUID = -7065491678170232623L;

        @Column(name = "time", timestamp = true)
        private Instant time;

        @Column(name = "hostname", tag = true)
        private String hostname;

        @Column(name = "value")
        private Double value;
    }
}
