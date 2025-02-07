package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.core.util.ClientCertificateProperties;
import org.apereo.cas.support.inwebo.service.response.InweboDeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.support.inwebo.service.soap.generated.LoginQueryResult;
import org.apereo.cas.support.inwebo.service.soap.generated.LoginSearchResult;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.ssl.SSLUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
class InweboServiceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final String OPERATION = "operation";

    private static final long COUNT = 1;
    private static final long USER_ID = 2;
    private static final long ACTIVATION_STATUS = 1;
    private static final long USER_STATUS = 4;

    private InweboService service;

    private int serviceApiPort;

    @BeforeEach
    void setUp() {
        val casProperties = new CasConfigurationProperties();

        val certificate = new SpringResourceProperties();
        certificate.setLocation(new ClassPathResource("clientcert.p12"));
        val clientCertificate = new ClientCertificateProperties();
        clientCertificate.setCertificate(certificate);
        clientCertificate.setPassphrase("password");

        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        inwebo.setClientCertificate(clientCertificate);

        serviceApiPort = RandomUtils.nextInt(4000, 9999);
        inwebo.setServiceApiUrl("http://localhost:" + serviceApiPort);

        inwebo.setConsoleAdminUrl("http://localhost:8288");

        val sslContext = SSLUtils.buildSSLContext(clientCertificate);
        service = new InweboService(casProperties, mock(InweboConsoleAdmin.class), sslContext);
        assertNotNull(service.getCasProperties());
        assertNotNull(service.getConsoleAdmin());
        assertNotNull(service.getContext());
        when(service.getConsoleAdmin().loginQuery(anyLong())).thenReturn(new LoginQueryResult());
    }

    @Test
    void verifyCallFails() {
        assertThrows(SSLHandshakeException.class, () -> service.call("https://untrusted-root.badssl.com/"));
    }

    @Test
    void verifyLoginSearch() {
        val result = new LoginSearchResult();
        result.setErr("OK");
        result.setCount(COUNT);
        result.getId().add(USER_ID);
        result.getActivationStatus().add(ACTIVATION_STATUS);
        result.getStatus().add(USER_STATUS);
        when(service.getConsoleAdmin().loginSearch(anyString())).thenReturn(result);

        val output = service.loginSearchQuery("login");
        assertNotNull(output);
        assertEquals(COUNT, output.getCount());
        assertEquals(USER_ID, output.getUserId());
        assertEquals(ACTIVATION_STATUS, output.getActivationStatus());
        assertEquals(USER_STATUS, output.getUserStatus());
    }

    @Test
    void verifyLoginSearchNoAuthenticatorForceBrowserAuthentication() {
        val loginSearchResult = new LoginSearchResult();
        loginSearchResult.setErr("OK");
        loginSearchResult.setCount(COUNT);
        loginSearchResult.getId().add(USER_ID);
        loginSearchResult.getActivationStatus().add(ACTIVATION_STATUS);
        loginSearchResult.getStatus().add(USER_STATUS);
        when(service.getConsoleAdmin().loginSearch(anyString())).thenReturn(loginSearchResult);

        val loginQueryResult = new LoginQueryResult();
        loginQueryResult.setErr("OK");
        when(service.getConsoleAdmin().loginQuery(anyLong())).thenReturn(loginQueryResult);

        val output = service.loginSearchQuery("login");
        assertNotNull(output);
        assertEquals(COUNT, output.getCount());
        assertEquals(USER_ID, output.getUserId());
        assertEquals(4, output.getActivationStatus());
        assertEquals(USER_STATUS, output.getUserStatus());
    }

    @Test
    void verifyLoginSearchOneAuthenticatorStayPushAuthentication() {
        val loginSearchResult = new LoginSearchResult();
        loginSearchResult.setErr("OK");
        loginSearchResult.setCount(COUNT);
        loginSearchResult.getId().add(USER_ID);
        loginSearchResult.getActivationStatus().add(ACTIVATION_STATUS);
        loginSearchResult.getStatus().add(USER_STATUS);
        when(service.getConsoleAdmin().loginSearch(anyString())).thenReturn(loginSearchResult);

        val loginQueryResult = new LoginQueryResult();
        loginQueryResult.setErr("OK");
        loginQueryResult.getManame().add("Authenticator");
        loginQueryResult.getManame().add(StringUtils.EMPTY);
        when(service.getConsoleAdmin().loginQuery(anyLong())).thenReturn(loginQueryResult);

        val output = service.loginSearchQuery("login");
        assertNotNull(output);
        assertEquals(COUNT, output.getCount());
        assertEquals(USER_ID, output.getUserId());
        assertEquals(ACTIVATION_STATUS, output.getActivationStatus());
        assertEquals(USER_STATUS, output.getUserStatus());
    }

    @Test
    void verifyLoginSearchOneAuthenticatorAmongSeveralForcePushAndBrowserAuthentication() {
        val loginSearchResult = new LoginSearchResult();
        loginSearchResult.setErr("OK");
        loginSearchResult.setCount(COUNT);
        loginSearchResult.getId().add(USER_ID);
        loginSearchResult.getActivationStatus().add(ACTIVATION_STATUS);
        loginSearchResult.getStatus().add(USER_STATUS);
        when(service.getConsoleAdmin().loginSearch(anyString())).thenReturn(loginSearchResult);

        val loginQueryResult = new LoginQueryResult();
        loginQueryResult.setErr("OK");
        loginQueryResult.getManame().add("Authenticator");
        loginQueryResult.getManame().add("SomethingElse");
        loginQueryResult.getManame().add(StringUtils.EMPTY);
        when(service.getConsoleAdmin().loginQuery(anyLong())).thenReturn(loginQueryResult);

        val output = service.loginSearchQuery("login");
        assertNotNull(output);
        assertEquals(COUNT, output.getCount());
        assertEquals(USER_ID, output.getUserId());
        assertEquals(5, output.getActivationStatus());
        assertEquals(USER_STATUS, output.getUserStatus());
    }

    @Test
    void verifyCheckPushResult() throws Throwable {
        val data = MAPPER.writeValueAsString(Map.of("err", "OK", "name", "Device", "sessionId", "123456"));
        try (val webServer = new MockWebServer(serviceApiPort,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertNotNull(service.checkPushResult("login", "sessionid"));
        }
    }

    @Test
    void verifyAuthExtended() throws Throwable {
        val data = MAPPER.writeValueAsString(Map.of("err", "OK", "name", "Device", "sessionId", "123456"));
        try (val webServer = new MockWebServer(serviceApiPort,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertNotNull(service.authenticateExtended("login", "sessionid"));
        }
    }

    @Test
    void verifyPush() throws Throwable {
        val data = MAPPER.writeValueAsString(Map.of("err", "OK", "name", "Device", "sessionId", "123456"));
        try (val webServer = new MockWebServer(serviceApiPort,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertNotNull(service.pushAuthenticate("login"));
        }
    }


    @Test
    void verifyErrOk() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "OK");
        assertEquals(InweboResult.OK, response.getResult());
    }

    @Test
    void verifyErrNopush() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:NOPUSH");
        assertEquals(InweboResult.NOPUSH, response.getResult());
    }

    @Test
    void verifyErrNoma() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:NOMA");
        assertEquals(InweboResult.NOMA, response.getResult());
    }

    @Test
    void verifyErrNologin() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:NOLOGIN");
        assertEquals(InweboResult.NOLOGIN, response.getResult());
    }

    @Test
    void verifyErrSn() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:SN");
        assertEquals(InweboResult.SN, response.getResult());
    }

    @Test
    void verifyErrSrv() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:srv unknown");
        assertEquals(InweboResult.UNKNOWN_SERVICE, response.getResult());
    }

    @Test
    void verifyErrWaiting() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:WAITING");
        assertEquals(InweboResult.WAITING, response.getResult());
    }

    @Test
    void verifyErrRefused() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:REFUSED");
        assertEquals(InweboResult.REFUSED, response.getResult());
    }

    @Test
    void verifyErrTimeout() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:TIMEOUT");
        assertEquals(InweboResult.TIMEOUT, response.getResult());
    }

    @Test
    void verifyErrOther() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:other");
        assertEquals(InweboResult.NOK, response.getResult());
    }
}
