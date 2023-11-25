package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BlackDotIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("RestfulApi")
class BlackDotIPAddressIntelligenceServiceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyBannedOperation() throws Throwable {
        try (val webServer = new MockWebServer(StringUtils.EMPTY, HttpStatus.TOO_MANY_REQUESTS)) {
            webServer.start();
            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getBlackDot().setUrl("http://localhost:" + webServer.getPort() + "?ip=%s");
            props.getIpIntel().getBlackDot().setEmailAddress("cas@apereo.org");
            val service = new BlackDotIPAddressIntelligenceService(props);
            val response = service.examine(MockRequestContext.create(), "37.58.59.181");
            assertTrue(response.isBanned());
        }

    }

    @Test
    void verifyErrorStatusOperation() throws Throwable {
        val data = MAPPER.writeValueAsString(Collections.singletonMap("status", "error"));
        try (val webServer = new MockWebServer(data)) {
            webServer.start();
            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getBlackDot().setUrl("http://localhost:" + webServer.getPort() + "?ip=%s");
            props.getIpIntel().getBlackDot().setEmailAddress("cas@apereo.org");
            val service = new BlackDotIPAddressIntelligenceService(props);
            val response = service.examine(MockRequestContext.create(), "37.58.59.181");
            assertTrue(response.isBanned());
        }
    }

    @Test
    void verifySuccessStatusAndBannedWithRank() throws Throwable {
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("status", "success", "result", 1));
        try (val webServer = new MockWebServer(data)) {
            webServer.start();
            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getBlackDot().setUrl("http://localhost:" + webServer.getPort() + "?ip=%s");
            props.getIpIntel().getBlackDot().setEmailAddress("cas@apereo.org");
            val service = new BlackDotIPAddressIntelligenceService(props);
            val response = service.examine(MockRequestContext.create(), "37.58.59.181");
            assertTrue(response.isBanned());
        }
    }

    @Test
    void verifySuccessStatus() throws Throwable {
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("status", "success", "result", 0));
        try (val webServer = new MockWebServer(data)) {
            webServer.start();
            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getBlackDot().setUrl("http://localhost:" + webServer.getPort() + "?ip=%s");
            props.getIpIntel().getBlackDot().setEmailAddress("cas@apereo.org");
            val service = new BlackDotIPAddressIntelligenceService(props);
            val response = service.examine(MockRequestContext.create(), "37.58.59.181");
            assertFalse(response.isBanned());
        }
    }

    @Test
    void verifySuccessStatusRanked() throws Throwable {
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("status", "success", "result", 0.4351));
        try (val webServer = new MockWebServer(data)) {
            webServer.start();
            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getBlackDot().setUrl("http://localhost:" + webServer.getPort() + "?ip=%s");
            props.getIpIntel().getBlackDot().setEmailAddress("cas@apereo.org");
            val service = new BlackDotIPAddressIntelligenceService(props);
            val response = service.examine(MockRequestContext.create(), "37.58.59.181");
            assertFalse(response.isBanned());
            assertEquals(0.4351, response.getScore());
        }
    }
    
    

    @Test
    void verifyBadResponse() throws Throwable {
        try (val webServer = new MockWebServer("${bad-json$")) {
            webServer.start();
            val props = new AdaptiveAuthenticationProperties();
            props.getIpIntel().getBlackDot().setUrl("http://localhost:" + webServer.getPort() + "?ip=%s");
            props.getIpIntel().getBlackDot().setMode("DYNA_CHECK");
            props.getIpIntel().getBlackDot().setEmailAddress("cas@apereo.org");
            val service = new BlackDotIPAddressIntelligenceService(props);
            val response = service.examine(MockRequestContext.create(), "37.58.59.181");
            assertTrue(response.isBanned());
        }
    }
}
