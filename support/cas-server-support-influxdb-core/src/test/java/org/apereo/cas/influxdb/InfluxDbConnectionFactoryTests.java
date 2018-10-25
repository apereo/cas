package org.apereo.cas.influxdb;

import org.apereo.cas.category.InfluxDbCategory;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import lombok.val;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.Point;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InfluxDbConnectionFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Category(InfluxDbCategory.class)
@EnabledIfContinuousIntegration
public class InfluxDbConnectionFactoryTests {

    private static final String CAS_EVENTS_DATABASE = "casEventsDatabase";

    private InfluxDbConnectionFactory factory;

    @BeforeEach
    public void init() {
        this.factory = new InfluxDbConnectionFactory("http://localhost:8086", "root",
            "root", CAS_EVENTS_DATABASE, true);
    }

    @AfterEach
    public void shutdown() {
        this.factory.close();
    }

    @Test
    public void verifyWritePoint() {
        val p = Point.measurement("events")
            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .addField("hostname", "cas.example.org")
            .build();
        factory.write(p, CAS_EVENTS_DATABASE);
        val result = factory.query("*", "events", CAS_EVENTS_DATABASE);
        val resultMapper = new InfluxDBResultMapper();
        val resultEvents = resultMapper.toPOJO(result, InfluxEvent.class);
        assertNotNull(resultEvents);
        assertEquals(1, resultEvents.size());
        assertEquals("cas.example.org", resultEvents.iterator().next().hostname);
    }

    @Measurement(name = "events")
    public static class InfluxEvent {
        @Column(name = "time")
        private Instant time;

        @Column(name = "hostname", tag = true)
        private String hostname;
    }
}
