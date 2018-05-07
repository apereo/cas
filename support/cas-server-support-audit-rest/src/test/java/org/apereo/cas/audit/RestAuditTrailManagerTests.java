package org.apereo.cas.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.configuration.model.core.audit.AuditRestProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.inspektr.audit.AuditActionContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * This is {@link RestAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilSerializationConfiguration.class
})
public class RestAuditTrailManagerTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyAction() throws Exception {
        final var props = new AuditRestProperties();
        props.setUrl("http://localhost:9296");
        final var r = new RestAuditTrailManager(props);
        r.setAsynchronous(false);

        final var audit = new AuditActionContext("casuser", "resource", "action",
            "CAS", new Date(), "123.456.789.000", "123.456.789.000");
        final var data = MAPPER.writeValueAsString(CollectionUtils.wrapSet(audit));
        try (var webServer = new MockWebServer(9296,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            r.record(audit);
            assertFalse(r.getAuditRecordsSince(LocalDate.now()).isEmpty());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
