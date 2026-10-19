package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.config.CasSupportRestAuditAutoConfiguration;
import org.apereo.cas.configuration.model.core.audit.AuditRestProperties;
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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasSupportRestAuditAutoConfiguration.class, properties = {
    "cas.audit.rest.url=https://localhost/audit",
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
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyRemoval() {
        assertNotNull(auditTrailManager);
        try (val webServer = new MockWebServer(
            new ByteArrayResource(ArrayUtils.EMPTY_BYTE_ARRAY), HttpStatus.OK)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            managerFor(webServer).removeAll();
        }
    }

    @Test
    void verifyGet() {
        val audit = new AuditActionContext("casuser", "resource", "action",
            "CAS", LocalDateTime.now(Clock.systemUTC()),
            new ClientInfo("123.456.789.000", "123.456.789.000", "GoogleChrome", "London"));
        val data = MAPPER.writeValueAsString(CollectionUtils.wrapSet(audit));

        try (val webServer = new MockWebServer(
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8)), HttpStatus.OK)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            val manager = managerFor(webServer);
            manager.record(audit);

            val time = LocalDateTime.now(ZoneOffset.UTC).minusDays(2);
            val criteria = new HashMap<AuditTrailManager.WhereClauseFields, Object>();
            criteria.put(AuditTrailManager.WhereClauseFields.DATE, time);
            criteria.put(AuditTrailManager.WhereClauseFields.PRINCIPAL, "casuser");
            criteria.put(AuditTrailManager.WhereClauseFields.COUNT, "10");
            val results = manager.getAuditRecords(criteria);
            assertFalse(results.isEmpty());
        }
    }

    @Test
    void verifyRecordsReadBackAsPosted() {
        val audit = new AuditActionContext("casuser", "resource", "action",
            "CAS", LocalDateTime.now(Clock.systemUTC()),
            new ClientInfo("123.456.789.000", "123.456.789.000", "GoogleChrome", "London")
                .setHeaders(Map.of("H1", "V1")));
        val serializer = new AuditActionContextJsonSerializer(applicationContext);
        val posted = serializer.toString(audit);
        val manager = new RestAuditTrailManager(serializer, new AuditRestProperties());
        val records = manager.readAuditRecords("[%s]".formatted(posted));
        assertEquals(1, records.size());
        assertEquals("casuser", records.getFirst().getPrincipal());
        assertEquals("V1", records.getFirst().getClientInfo().getHeaders().get("H1"));
        val wrapped = manager.readAuditRecords("[\"java.util.ArrayList\",[%s]]".formatted(posted));
        assertEquals(1, wrapped.size());
        val plain = manager.readAuditRecords(MAPPER.writeValueAsString(List.of(audit)));
        assertEquals("resource", plain.getFirst().getResourceOperatedUpon());
    }

    private AuditTrailManager managerFor(final MockWebServer webServer) {
        val properties = new AuditRestProperties();
        properties.setAsynchronous(false);
        properties.setUrl("http://localhost:%s".formatted(webServer.getPort()));
        return new RestAuditTrailManager(new AuditActionContextJsonSerializer(applicationContext), properties);
    }
}
