package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJdbcAuditAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.thread.Cleanable;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * This is {@link CasJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(
    classes = {
        BaseAuditConfigurationTests.SharedTestConfiguration.class,
        CasJdbcAuditAutoConfiguration.class,
        CasHibernateJpaAutoConfiguration.class
    },
    properties = {
        "cas.jdbc.show-sql=true",
        "cas.audit.jdbc.column-length=-1",
        "cas.audit.jdbc.schedule.enabled=true",
        "cas.audit.jdbc.asynchronous=false"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Getter
@Tag("JDBC")
@ExtendWith(CasTestExtension.class)
class CasJdbcAuditConfigurationTests extends BaseAuditConfigurationTests {

    @Autowired
    @Qualifier("inspektrAuditTrailCleaner")
    private Cleanable inspektrAuditTrailCleaner;

    @Autowired
    @Qualifier("jdbcAuditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Test
    void verifyCleaner() {
        inspektrAuditTrailCleaner.clean();
    }

    @Test
    void verifyLargeResource() {
        val headers = new HashMap<String, String>();
        IntStream.rangeClosed(1, 100).forEach(i -> headers.put(
            i + "-" + UUID.randomUUID(),
            RandomUtils.randomAlphanumeric(500)));
        val clientInfo = new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London")
            .setExtraInfo(Map.of("Hello", "World"))
            .setHeaders(headers);
        val context = new AuditActionContext(
            UUID.randomUUID().toString(),
            RandomUtils.randomAlphabetic(10_000),
            "TEST",
            "CAS", LocalDateTime.now(Clock.systemUTC()),
            clientInfo);
        getAuditTrailManager().record(context);
    }
}
