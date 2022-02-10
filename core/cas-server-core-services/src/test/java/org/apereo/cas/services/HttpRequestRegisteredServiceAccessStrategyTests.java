package org.apereo.cas.services;

import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HttpRequestRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("RegisteredService")
public class HttpRequestRegisteredServiceAccessStrategyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "HttpRequestRegisteredServiceAccessStrategy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @BeforeEach
    public void setup() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("192.861.151.163");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome/Mozilla");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }

    @Test
    public void verifySerialization() throws IOException {
        val strategyWritten = new HttpRequestRegisteredServiceAccessStrategy();
        strategyWritten.setIpAddress("129.+.123.\\d\\d");
        strategyWritten.setUserAgent("Google Chrome (Firefox)");
        MAPPER.writeValue(JSON_FILE, strategyWritten);
        val read = MAPPER.readValue(JSON_FILE, RegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, read);
        assertNotNull(read.toString());
    }

    @Test
    public void verifyAccessByIp() {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        policy.setIpAddress("192.\\d\\d\\d.\\d\\d\\d.163");
        assertTrue(policy.isServiceAccessAllowed());
    }

    @Test
    public void verifyUserAgentAccess() {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        policy.setUserAgent(".*moz.*");
        assertTrue(policy.isServiceAccessAllowed());
    }

    @Test
    public void verifyMatchFailsByIp() {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        policy.setIpAddress("123.456.789.111");
        assertFalse(policy.isServiceAccessAllowed());
    }

    @Test
    public void verifyUndefinedValues() {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        assertTrue(policy.isServiceAccessAllowed());
    }

    @Test
    public void verifyAllFieldsPresent() {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        policy.setUserAgent(".*moz.*");
        policy.setIpAddress(".*861.*");
        assertTrue(policy.isServiceAccessAllowed());
    }
}
