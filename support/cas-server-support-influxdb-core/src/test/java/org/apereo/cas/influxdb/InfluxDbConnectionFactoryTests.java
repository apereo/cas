package org.apereo.cas.influxdb;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InfluxDbConnectionFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("InfluxDb")
@EnabledIfListeningOnPort(port = 8181)
@SpringBootTestAutoConfigurations
@SpringBootTest(
    classes = RefreshAutoConfiguration.class,
    properties = {
        "cas.events.influx-db.database=CasEventsDatabase",
        "cas.events.influx-db.token=${#systemProperties['java.io.tmpdir']}/.influxdb-token",
        "cas.events.influx-db.url=http://localhost:8181"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class InfluxDbConnectionFactoryTests {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Test
    void verifyWritePoint() {
        val factory = new InfluxDbConnectionFactory(casProperties.getEvents().getInfluxDb());
        val measurement = "events%s".formatted(RandomUtils.nextLong());
        factory.write(measurement,
            Map.of("number", 1234.5678, "flag", true, "name", "ApereoCAS"),
            Map.of("hostname", "cas.example.org"));
        val result = factory.query(measurement).toList();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        val first = result.getFirst();
        assertEquals("cas.example.org", first.getTag("hostname"));
        assertEquals(1234.5678, first.getFloatField("number"));
        assertEquals(true, first.getBooleanField("flag"));
        assertEquals("ApereoCAS", first.getStringField("name"));
        factory.close();
    }
}
