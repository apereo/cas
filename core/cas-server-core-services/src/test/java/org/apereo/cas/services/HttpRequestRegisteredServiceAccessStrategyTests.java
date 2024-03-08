package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HttpRequestRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("RegisteredService")
class HttpRequestRegisteredServiceAccessStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @BeforeEach
    public void setup() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("192.861.151.163");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome/Mozilla");
        request.addHeader("CustomHeader", "abcd-12-xyz#");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifySerialization() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val strategyWritten = new HttpRequestRegisteredServiceAccessStrategy();
        strategyWritten.setIpAddress("129.+.123.\\d\\d");
        strategyWritten.setUserAgent("Google Chrome (Firefox)");
        strategyWritten.setHeaders(CollectionUtils.wrap("Header1", "Value.+Pattern"));
        MAPPER.writeValue(jsonFile, strategyWritten);
        val read = MAPPER.readValue(jsonFile, RegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, read);
        assertNotNull(read.toString());
    }

    @Test
    void verifyAccessByIp() throws Throwable {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        policy.setIpAddress("192.\\d\\d\\d.\\d\\d\\d.163");
        assertTrue(policy.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void verifyAccessByIpAndHeader() throws Throwable {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        policy.setIpAddress("192.\\d\\d\\d.\\d\\d\\d.163");
        policy.setHeaders(Map.of("CustomHeader", "^abcd-\\d\\d-.+#"));
        assertTrue(policy.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void verifyUserAgentAccess() throws Throwable {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        policy.setUserAgent(".*moz.*");
        assertTrue(policy.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void verifyMatchFailsByIp() throws Throwable {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        policy.setIpAddress("123.456.789.111");
        assertFalse(policy.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void verifyUndefinedValues() throws Throwable {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        assertTrue(policy.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void verifyAllFieldsPresent() throws Throwable {
        val policy = new HttpRequestRegisteredServiceAccessStrategy();
        policy.setUserAgent(".*moz.*");
        policy.setIpAddress(".*861.*");
        assertTrue(policy.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }
}
