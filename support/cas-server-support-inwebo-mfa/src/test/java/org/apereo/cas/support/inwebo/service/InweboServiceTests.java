package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.core.util.ClientCertificateProperties;
import org.apereo.cas.support.inwebo.service.response.InweboDeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.support.inwebo.service.soap.generated.LoginSearchResult;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.ssl.SSLUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

import javax.net.ssl.SSLHandshakeException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link InweboService}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("MFAProvider")
public class InweboServiceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final String OPERATION = "operation";

    private InweboService service;

    @BeforeEach
    public void setUp() {
        val casProperties = new CasConfigurationProperties();

        val certificate = new SpringResourceProperties();
        certificate.setLocation(new ClassPathResource("clientcert.p12"));
        val clientCertificate = new ClientCertificateProperties();
        clientCertificate.setCertificate(certificate);
        clientCertificate.setPassphrase("password");
        
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        inwebo.setClientCertificate(clientCertificate);
        inwebo.setServiceApiUrl("http://localhost:8282");
        inwebo.setConsoleAdminUrl("http://localhost:8288");

        val sslContext = SSLUtils.buildSSLContext(clientCertificate);
        service = new InweboService(casProperties, mock(InweboConsoleAdmin.class), sslContext);
        assertNotNull(service.getCasProperties());
        assertNotNull(service.getConsoleAdmin());
        assertNotNull(service.getContext());
    }

    @Test
    public void verifyCallFails() {
        assertThrows(SSLHandshakeException.class, () -> service.call("https://untrusted-root.badssl.com/"));
    }

    @Test
    public void verifyLoginSearch() {
        val result = new LoginSearchResult();
        result.setErr("OK");
        result.setCount(1);
        result.setN(1);
        result.getId().add(1L);
        result.getActivationStatus().add(1L);
        result.getStatus().add(1L);

        when(service.getConsoleAdmin().loginSearch(anyString())).thenReturn(result);
        assertNotNull(service.loginSearch("login"));
    }

    @Test
    public void verifyCheckPushResult() throws Exception {
        val data = MAPPER.writeValueAsString(Map.of("err", "OK", "name", "Device", "sessionId", "123456"));
        try (val webServer = new MockWebServer(8282,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertNotNull(service.checkPushResult("login", "sessionid"));
        }
    }

    @Test
    public void verifyAuthExtended() throws Exception {
        val data = MAPPER.writeValueAsString(Map.of("err", "OK", "name", "Device", "sessionId", "123456"));
        try (val webServer = new MockWebServer(8282,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertNotNull(service.authenticateExtended("login", "sessionid"));
        }
    }

    @Test
    public void verifyPush() throws Exception {
        val data = MAPPER.writeValueAsString(Map.of("err", "OK", "name", "Device", "sessionId", "123456"));
        try (val webServer = new MockWebServer(8282,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertNotNull(service.pushAuthenticate("login"));
        }
    }


    @Test
    public void verifyErrOk() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "OK");
        assertEquals(InweboResult.OK, response.getResult());
    }

    @Test
    public void verifyErrNopush() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:NOPUSH");
        assertEquals(InweboResult.NOPUSH, response.getResult());
    }

    @Test
    public void verifyErrNoma() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:NOMA");
        assertEquals(InweboResult.NOMA, response.getResult());
    }

    @Test
    public void verifyErrNologin() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:NOLOGIN");
        assertEquals(InweboResult.NOLOGIN, response.getResult());
    }

    @Test
    public void verifyErrSn() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:SN");
        assertEquals(InweboResult.SN, response.getResult());
    }

    @Test
    public void verifyErrSrv() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:srv unknown");
        assertEquals(InweboResult.UNKNOWN_SERVICE, response.getResult());
    }

    @Test
    public void verifyErrWaiting() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:WAITING");
        assertEquals(InweboResult.WAITING, response.getResult());
    }

    @Test
    public void verifyErrRefused() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:REFUSED");
        assertEquals(InweboResult.REFUSED, response.getResult());
    }

    @Test
    public void verifyErrTimeout() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:TIMEOUT");
        assertEquals(InweboResult.TIMEOUT, response.getResult());
    }

    @Test
    public void verifyErrOther() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:other");
        assertEquals(InweboResult.NOK, response.getResult());
    }
}
