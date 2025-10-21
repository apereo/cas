package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.support.interrupt.RestfulInterruptProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestEndpointInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class RestEndpointInterruptInquirerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private MockWebServer webServer;

    @BeforeEach
    void initialize() throws Exception {
        val response = new InterruptResponse();
        response.setSsoEnabled(true);
        response.setInterrupt(true);
        response.setBlock(true);
        response.setMessage(getClass().getSimpleName());
        response.setLinks(CollectionUtils.wrap("text1", "link1", "text2", "link2"));

        val data = JsonMapper.builder()
            .configure(EnumFeature.READ_ENUMS_USING_TO_STRING, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .changeDefaultPropertyInclusion(handler -> {
                handler.withValueInclusion(JsonInclude.Include.NON_NULL);
                handler.withContentInclusion(JsonInclude.Include.NON_NULL);
                return handler;
            })
            .build()
            .writeValueAsString(response);
        this.webServer = new MockWebServer(8888,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        this.webServer.start();
    }

    @AfterEach
    public void stop() {
        webServer.stop();
    }

    @Test
    void verifyResponseCanBeFoundFromRest() throws Throwable {
        val restProps = new RestfulInterruptProperties();
        restProps.setUrl("http://localhost:8888");
        val context = MockRequestContext.create(applicationContext);
        context.addHeader("accept-language", "fr");
        val q = new RestEndpointInterruptInquirer(restProps);
        val response = q.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            context);
        assertNotNull(response);
        assertTrue(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertEquals(2, response.getLinks().size());
        assertEquals(getClass().getSimpleName(), response.getMessage());
    }

    @Test
    void verifyBadAttempt() throws Throwable {
        val restProps = new RestfulInterruptProperties();
        restProps.setUrl("http://localhost:8888");
        val context = MockRequestContext.create(applicationContext);
        val q = new RestEndpointInterruptInquirer(restProps);
        val response = q.inquire(null,
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            context);
        assertFalse(response.isInterrupt());
    }
}
