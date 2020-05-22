package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;

import com.duosecurity.client.Http;
import com.duosecurity.duoweb.DuoWebException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BasicDuoSecurityAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.authn.mfa.duo[0].duo-secret-key=1234567890",
    "cas.authn.mfa.duo[0].duo-application-key=abcdefghijklmnop",
    "cas.authn.mfa.duo[0].duo-integration-key=QRSTUVWXYZ",
    "cas.authn.mfa.duo[0].duo-api-host=httpbin.org/post"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFA")
public class BasicDuoSecurityAuthenticationServiceTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private HttpClient httpClient;

    @Test
    public void verifySign() {
        val service = new BasicDuoSecurityAuthenticationService(casProperties.getAuthn().getMfa().getDuo().get(0), httpClient);
        assertNotNull(service.signRequestToken("casuser"));
    }

    @Test
    public void verifyAuthN() {
        val service = new BasicDuoSecurityAuthenticationService(casProperties.getAuthn().getMfa().getDuo().get(0), httpClient);
        val token = service.signRequestToken("casuser");
        val creds = new DuoSecurityCredential("casuser", token + ":casuser", "mfa-duo");
        assertThrows(DuoWebException.class, () -> service.authenticate(creds));
    }

    @Test
    public void verifyAuthNNoToken() {
        val service = new BasicDuoSecurityAuthenticationService(casProperties.getAuthn().getMfa().getDuo().get(0), httpClient);
        val creds = new DuoSecurityCredential("casuser", StringUtils.EMPTY, "mfa-duo");
        assertThrows(IllegalArgumentException.class, () -> service.authenticate(creds));
    }

    @Test
    public void verifyAuthNDirect() throws Exception {
        val service = new BasicDuoSecurityAuthenticationService(casProperties.getAuthn().getMfa().getDuo().get(0), httpClient);
        try (val webServer = new MockWebServer(6342)) {
            webServer.start();
            val creds = new DuoSecurityDirectCredential(RegisteredServiceTestUtils.getAuthentication(), "mfa-duo");
            assertFalse(service.authenticate(creds).getKey());
        }
    }

    @Test
    public void verifyGetAccountNoStat() {
        val props = casProperties.getAuthn().getMfa().getDuo().get(0);
        val service = new BasicDuoSecurityAuthenticationService(props, httpClient) {
            private static final long serialVersionUID = 6245462449489284549L;

            @Override
            protected String getHttpResponse(final Http userRequest) throws Exception {
                return MAPPER.writeValueAsString(Map.of("response", "pong"));
            }
        };
        assertEquals(DuoSecurityUserAccountStatus.UNAVAILABLE, service.getUserAccount("casuser").getStatus());
        assertEquals(DuoSecurityUserAccountStatus.UNAVAILABLE, service.getUserAccount("casuser").getStatus());
    }

    @Test
    public void verifyGetAccountEnroll() {
        val props = casProperties.getAuthn().getMfa().getDuo().get(0);
        val service = new BasicDuoSecurityAuthenticationService(props, httpClient) {
            private static final long serialVersionUID = 6245462449489284549L;

            @Override
            protected String getHttpResponse(final Http userRequest) throws Exception {
                val response = Map.of("status_msg", "OK", "result", "ENROLL", "enroll_portal_url", "google.com");
                return MAPPER.writeValueAsString(Map.of(
                    "stat", "OK",
                    "response", response));
            }
        };
        assertEquals(DuoSecurityUserAccountStatus.ENROLL, service.getUserAccount("casuser").getStatus());
    }

    @Test
    public void verifyGetAccountFail() {
        val props = casProperties.getAuthn().getMfa().getDuo().get(0);
        val service = new BasicDuoSecurityAuthenticationService(props, httpClient) {
            private static final long serialVersionUID = 6245462449489284549L;

            @Override
            protected String getHttpResponse(final Http userRequest) throws Exception {
                return MAPPER.writeValueAsString(Map.of(
                    "stat", "FAIL",
                    "code", "100000"));
            }
        };
        assertEquals(DuoSecurityUserAccountStatus.UNAVAILABLE, service.getUserAccount("casuser").getStatus());
    }

    @Test
    public void verifyGetAccountAuth() {
        val props = casProperties.getAuthn().getMfa().getDuo().get(0);
        val service = new BasicDuoSecurityAuthenticationService(props, httpClient) {
            private static final long serialVersionUID = 6245462449489284549L;

            @Override
            protected String getHttpResponse(final Http userRequest) throws Exception {
                return MAPPER.writeValueAsString(Map.of(
                    "stat", "FAIL",
                    "code", "1000"));
            }
        };
        assertEquals(DuoSecurityUserAccountStatus.AUTH, service.getUserAccount("casuser").getStatus());
    }
}
