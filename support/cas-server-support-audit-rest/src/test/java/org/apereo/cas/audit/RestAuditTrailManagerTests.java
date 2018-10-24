package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.config.CasSupportRestAuditConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * This is {@link RestAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasSupportRestAuditConfiguration.class,
    CasCoreUtilSerializationConfiguration.class
})
@Category(RestfulApiCategory.class)
@Slf4j
@TestPropertySource(properties = {
    "cas.audit.rest.url=http://localhost:9296",
    "cas.audit.rest.asynchronous=false"
})
@Getter
public class RestAuditTrailManagerTests extends BaseAuditConfigurationTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final String DATA;

    static {
        val audit = new AuditActionContext("casuser", "resource", "action",
            "CAS", new Date(), "123.456.789.000", "123.456.789.000");
        try {
            DATA = MAPPER.writeValueAsString(CollectionUtils.wrapSet(audit));
            LOGGER.debug("DATA: [{}]", DATA);
        } catch (final JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    @Autowired
    @Qualifier("restAuditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Test
    @Override
    public void verifyAuditManager() {
        try (val webServer = new MockWebServer(9296,
            new ByteArrayResource(DATA.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            super.verifyAuditManager();
        }
    }
}
