package org.apereo.cas.interrupt;

import module java.base;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
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
    void initialize() {
        val response = new InterruptResponse();
        response.setSsoEnabled(true);
        response.setInterrupt(true);
        response.setBlock(true);
        response.setMessage(getClass().getSimpleName());
        response.setLinks(CollectionUtils.wrap("text1", "link1", "text2", "link2"));

        val data = JsonMapper.builder()
            .configure(EnumFeature.READ_ENUMS_USING_TO_STRING, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .changeDefaultPropertyInclusion(handler -> handler.withValueInclusion(JsonInclude.Include.NON_NULL)
                .withContentInclusion(JsonInclude.Include.NON_NULL))
            .build()
            .writeValueAsString(response);
        this.webServer = new MockWebServer(
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
        restProps.setUrl("http://localhost:%s".formatted(webServer.getPort()));
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

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "NOT_FOUND", "INTERNAL_SERVER_ERROR"})
    void verifyErrorResponseDoesNotInterrupt(final HttpStatus status) throws Throwable {
        val body = """
            {"status": %s, "error": "%s", "path": "/"}
            """.formatted(status.value(), status.getReasonPhrase());
        try (val errorServer = new MockWebServer(body, status)) {
            errorServer.start();
            val restProps = new RestfulInterruptProperties();
            restProps.setUrl("http://localhost:%s".formatted(errorServer.getPort()));
            val context = MockRequestContext.create(applicationContext);
            val response = new RestEndpointInterruptInquirer(restProps).inquire(
                CoreAuthenticationTestUtils.getAuthentication("casuser"),
                CoreAuthenticationTestUtils.getRegisteredService(),
                CoreAuthenticationTestUtils.getService(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                context);
            assertFalse(response.isInterrupt());
        }
    }

    @Test
    void verifyBadAttempt() throws Throwable {
        val restProps = new RestfulInterruptProperties();
        restProps.setUrl("http://localhost:%s".formatted(webServer.getPort()));
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
