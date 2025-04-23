package org.apereo.cas.support.events.dao;

import org.apereo.cas.config.CasEventsInfluxDbRepositoryAutoConfiguration;
import org.apereo.cas.influxdb.InfluxDbConnectionFactory;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Duration;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InfluxDbCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasEventsInfluxDbRepositoryAutoConfiguration.class,
    properties = {
        "cas.events.influx-db.database=CasInfluxDbEvents",
        "cas.events.influx-db.token=${#systemProperties['java.io.tmpdir']}/.influxdb-token",
        "cas.events.influx-db.url=http://localhost:8181"
    })
@Tag("InfluxDb")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 8181)
@Getter
@Execution(ExecutionMode.SAME_THREAD)
class InfluxDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    @Qualifier("influxDbEventsConnectionFactory")
    private InfluxDbConnectionFactory influxDbEventsConnectionFactory;

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository eventRepository;

    @Test
    void verifyAggregates() throws Throwable {
        val dto1 = getCasEvent(UUID.randomUUID().toString());
        eventRepository.save(dto1);

        var results = eventRepository.aggregate().toList();
        assertFalse(results.isEmpty());

        results = eventRepository.aggregate(CasTicketGrantingTicketCreatedEvent.class, Duration.ofDays(2)).toList();
        assertFalse(results.isEmpty());
    }
}
