package org.apereo.cas.influxdb;

import org.apereo.cas.category.InfluxDbCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.Point;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.After;
import org.junit.Before;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;

/**
 * This is {@link InfluxDbConnectionFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Category(InfluxDbCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class InfluxDbConnectionFactoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final String CAS_EVENTS_DATABASE = "casEventsDatabase";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    private InfluxDbConnectionFactory factory;

    @Before
    public void init() {
        this.factory = new InfluxDbConnectionFactory("http://localhost:8086", "root",
            "root", CAS_EVENTS_DATABASE, true);
    }

    @After
    public void shutdown() {
        this.factory.close();
    }

    @Test
    public void verifyWritePoint() {
        final var p = Point.measurement("events")
            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .addField("hostname", "cas.example.org")
            .build();
        factory.write(p, CAS_EVENTS_DATABASE);
        final var result = factory.query("*", "events", CAS_EVENTS_DATABASE);
        final var resultMapper = new InfluxDBResultMapper();
        final var resultEvents = resultMapper.toPOJO(result, InfluxEvent.class);
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
