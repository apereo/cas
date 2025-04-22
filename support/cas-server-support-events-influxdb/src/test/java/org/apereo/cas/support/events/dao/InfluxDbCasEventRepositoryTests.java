package org.apereo.cas.support.events.dao;

import org.apereo.cas.config.CasEventsInfluxDbRepositoryAutoConfiguration;
import org.apereo.cas.influxdb.InfluxDbConnectionFactory;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

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
}
