package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.duosecurity.client.Http;
import com.duosecurity.duoweb.DuoWebException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

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
@Tag("MFAProvider")
public class BasicDuoSecurityAuthenticationServiceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private HttpClient httpClient;

    @Test
    public void verifySign() {
        val service = new BasicDuoSecurityAuthenticationService(casProperties.getAuthn().getMfa().getDuo().get(0),
            httpClient, List.of(MultifactorAuthenticationPrincipalResolver.identical()), Caffeine.newBuilder().build());
        assertTrue(service.getDuoClient().isEmpty());
        assertTrue(service.signRequestToken("casuser").isPresent());
    }

    @Test
    public void verifyAuthN() {
        val service = new BasicDuoSecurityAuthenticationService(casProperties.getAuthn().getMfa().getDuo().get(0),
            httpClient, List.of(MultifactorAuthenticationPrincipalResolver.identical()), Caffeine.newBuilder().build());
        assertTrue(service.getDuoClient().isEmpty());
        val token = service.signRequestToken("casuser").get();
        val creds = new DuoSecurityCredential("casuser", token + ":casuser", "mfa-duo");
        assertThrows(DuoWebException.class, () -> service.authenticate(creds));
    }

    @Test
    public void verifyAuthNNoToken() {
        val service = new BasicDuoSecurityAuthenticationService(casProperties.getAuthn().getMfa().getDuo().get(0),
            httpClient, List.of(MultifactorAuthenticationPrincipalResolver.identical()), Caffeine.newBuilder().build());
        assertTrue(service.getDuoClient().isEmpty());
        val creds = new DuoSecurityCredential("casuser", StringUtils.EMPTY, "mfa-duo");
        assertThrows(IllegalArgumentException.class, () -> service.authenticate(creds));
    }

    @Test
    public void verifyAuthNDirect() throws Exception {
        val service = new BasicDuoSecurityAuthenticationService(casProperties.getAuthn().getMfa().getDuo().get(0),
            httpClient, List.of(MultifactorAuthenticationPrincipalResolver.identical()), Caffeine.newBuilder().build());
        try (val webServer = new MockWebServer(6342)) {
            webServer.start();
            val creds = new DuoSecurityDirectCredential(RegisteredServiceTestUtils.getAuthentication().getPrincipal(), "mfa-duo");
            assertFalse(service.authenticate(creds).isSuccess());
        }
    }

    @Test
    public void verifyPasscodeFails() throws Exception {
        val service = new BasicDuoSecurityAuthenticationService(casProperties.getAuthn().getMfa().getDuo().get(0),
            httpClient, List.of(MultifactorAuthenticationPrincipalResolver.identical()), Caffeine.newBuilder().build());
        val creds = new DuoSecurityPasscodeCredential("casuser", "046573", "mfa-duo");
        assertFalse(service.authenticate(creds).isSuccess());
    }

    @Test
    public void verifyPasscode() throws Exception {
        val props = new DuoSecurityMultifactorAuthenticationProperties();
        BeanUtils.copyProperties(props, casProperties.getAuthn().getMfa().getDuo().get(0));
        props.setDuoApiHost("localhost:6342");
        val service = new BasicDuoSecurityAuthenticationService(props,
            httpClient, List.of(MultifactorAuthenticationPrincipalResolver.identical()), Caffeine.newBuilder().build()) {
            private static final long serialVersionUID = 1756840642345094968L;

            @Override
            protected JSONObject executeDuoApiRequest(final Http request) {
                return new JSONObject(Map.of("stat", "OK", "result", "allow"));
            }
        };

        try (val webServer = new MockWebServer(6342)) {
            webServer.start();
            val creds = new DuoSecurityPasscodeCredential("casuser", "123456", "mfa-duo");
            assertTrue(service.authenticate(creds).isSuccess());
        }
    }

    @Test
    public void verifyAccountStatusDisabled() throws Exception {
        val props = new DuoSecurityMultifactorAuthenticationProperties();
        BeanUtils.copyProperties(props, casProperties.getAuthn().getMfa().getDuo().get(0));
        props.setAccountStatusEnabled(false);
        val service = new BasicDuoSecurityAuthenticationService(props, httpClient,
            List.of(MultifactorAuthenticationPrincipalResolver.identical()),
            Caffeine.newBuilder().build());
        assertEquals(DuoSecurityUserAccountStatus.AUTH, service.getUserAccount("casuser").getStatus());
    }


    @Test
    public void verifyGetAccountNoStat() {
        val props = casProperties.getAuthn().getMfa().getDuo().get(0);
        val service = new BasicDuoSecurityAuthenticationService(props, httpClient,
            List.of(MultifactorAuthenticationPrincipalResolver.identical()), Caffeine.newBuilder().build()) {
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
        val service = new BasicDuoSecurityAuthenticationService(props, httpClient,
            List.of(MultifactorAuthenticationPrincipalResolver.identical()),
            Caffeine.newBuilder().build()) {
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
    public void verifyGetAccountFail() throws Exception {
        val props = casProperties.getAuthn().getMfa().getDuo().get(0);
        val service = new BasicDuoSecurityAuthenticationService(props, httpClient,
            List.of(MultifactorAuthenticationPrincipalResolver.identical()),
            Caffeine.newBuilder().build()) {
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
        val service = new BasicDuoSecurityAuthenticationService(props, httpClient,
            List.of(MultifactorAuthenticationPrincipalResolver.identical()),
            Caffeine.newBuilder().build()) {
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

    @Test
    public void verifyPing() throws Exception {
        var entity = MAPPER.writeValueAsString(Map.of("stat", "OK", "response", "pong"));
        try (val webServer = new MockWebServer(9310,
            new ByteArrayResource(entity.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            val props = new DuoSecurityMultifactorAuthenticationProperties().setDuoApiHost("http://localhost:9310");
            val service = new BasicDuoSecurityAuthenticationService(props, httpClient,
                List.of(MultifactorAuthenticationPrincipalResolver.identical()),
                Caffeine.newBuilder().build());
            assertTrue(service.ping());
        }
    }
}
