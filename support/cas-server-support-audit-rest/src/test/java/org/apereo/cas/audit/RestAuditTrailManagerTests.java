package org.apereo.cas.audit;

import org.apereo.cas.config.CasSupportRestAuditConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasSupportRestAuditConfiguration.class
}, properties = {
    "cas.audit.rest.url=http://localhost:9296",
    "cas.audit.rest.asynchronous=false"
})
@Tag("RestfulApi")
@Getter
@SuppressWarnings("JavaUtilDate")
public class RestAuditTrailManagerTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier("restAuditTrailManager")
    private AuditTrailManager auditTrailManager;


    @Test
    public void verifyRemoval() {
        try (val webServer = new MockWebServer(9296,
            new ByteArrayResource(ArrayUtils.EMPTY_BYTE_ARRAY), HttpStatus.OK)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            auditTrailManager.removeAll();
        }
    }

    @Test
    public void verifyGet() throws Exception {
        val audit = new AuditActionContext("casuser", "resource", "action",
            "CAS", new Date(), "123.456.789.000", "123.456.789.000", "GoogleChrome");
        val data = MAPPER.writeValueAsString(CollectionUtils.wrapSet(audit));

        try (val webServer = new MockWebServer(9296,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8)), HttpStatus.OK)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            auditTrailManager.record(audit);

            val time = LocalDate.now(ZoneOffset.UTC).minusDays(2);
            val results = auditTrailManager.getAuditRecordsSince(time);
            assertFalse(results.isEmpty());
        }
    }
}
