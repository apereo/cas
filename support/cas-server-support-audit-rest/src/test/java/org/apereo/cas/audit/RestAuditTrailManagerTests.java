package org.apereo.cas.audit;

import org.apereo.cas.config.CasSupportRestAuditAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasSupportRestAuditAutoConfiguration.class, properties = {
    "cas.audit.rest.url=http://localhost:${random.int[3000,9000]}",
    "cas.audit.rest.asynchronous=false"
})
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@Getter
class RestAuditTrailManagerTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier("restAuditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyRemoval() {
        val props = casProperties.getAudit().getRest();
        val port = URI.create(props.getUrl()).getPort();
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(ArrayUtils.EMPTY_BYTE_ARRAY), HttpStatus.OK)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            auditTrailManager.removeAll();
        }
    }

    @Test
    void verifyGet() throws Throwable {
        val audit = new AuditActionContext("casuser", "resource", "action",
            "CAS", LocalDateTime.now(Clock.systemUTC()),
            new ClientInfo("123.456.789.000", "123.456.789.000", "GoogleChrome", "London"));
        val data = MAPPER.writeValueAsString(CollectionUtils.wrapSet(audit));

        val props = casProperties.getAudit().getRest();
        val port = URI.create(props.getUrl()).getPort();
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8)), HttpStatus.OK)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            auditTrailManager.record(audit);

            val time = LocalDateTime.now(ZoneOffset.UTC).minusDays(2);
            val criteria = new HashMap<AuditTrailManager.WhereClauseFields, Object>();
            criteria.put(AuditTrailManager.WhereClauseFields.DATE, time);
            criteria.put(AuditTrailManager.WhereClauseFields.PRINCIPAL, "casuser");
            criteria.put(AuditTrailManager.WhereClauseFields.COUNT, "10");
            val results = auditTrailManager.getAuditRecords(criteria);
            assertFalse(results.isEmpty());
        }
    }
}
