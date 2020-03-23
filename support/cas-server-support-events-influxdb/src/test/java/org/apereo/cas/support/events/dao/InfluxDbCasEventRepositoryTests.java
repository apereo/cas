package org.apereo.cas.support.events.dao;

import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.config.CasEventsInfluxDbRepositoryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link InfluxDbCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasEventsInfluxDbRepositoryConfiguration.class
}, properties = "cas.events.influxDb.batchInterval=PT0.001S")
@Tag("InfluxDb")
@EnabledIfPortOpen(port = 8086)
@Getter
public class InfluxDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository eventRepository;
}
