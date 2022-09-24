package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

import javax.security.auth.login.FailedLoginException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulCasSimpleMultifactorAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.simple.token.rest.url=http://localhost:9229",
        "cas.authn.mfa.simple.token.rest.headers.h1=h2"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RestfulCasSimpleMultifactorAuthenticationServiceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleValueAsArray(true).defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier(CasSimpleMultifactorAuthenticationService.BEAN_NAME)
    private CasSimpleMultifactorAuthenticationService multifactorAuthenticationService;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    private TicketFactory defaultTicketFactory;

    @Test
    public void verifyGenerateToken() throws Exception {
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser");
        val tokenId = UUID.randomUUID().toString();
        val service = RegisteredServiceTestUtils.getService();
        try (val webServer = new MockWebServer(9229,
            new ByteArrayResource(tokenId.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val token = multifactorAuthenticationService.generate(authentication.getPrincipal(), service);
            assertEquals(token.getId(), tokenId);
            assertEquals(token.getService(), service);
        }

        try (val webServer = new MockWebServer(9229,
            new ByteArrayResource(tokenId.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            assertThrows(FailedLoginException.class, () -> multifactorAuthenticationService.generate(authentication.getPrincipal(), service));
        }
    }

    @Test
    public void verifyStoreToken() throws Exception {
        val tokenId = UUID.randomUUID().toString();
        val service = RegisteredServiceTestUtils.getService();
        try (val webServer = new MockWebServer(9229,
            new ByteArrayResource(tokenId.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.CREATED)) {
            webServer.start();
            val mfaFactory = (CasSimpleMultifactorAuthenticationTicketFactory)
                defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
            var token = (CasSimpleMultifactorAuthenticationTicket) mfaFactory.create(tokenId, service, Map.of());
            assertDoesNotThrow(() -> multifactorAuthenticationService.store(token));
        }
        try (val webServer = new MockWebServer(9229,
            new ByteArrayResource(tokenId.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            val mfaFactory = (CasSimpleMultifactorAuthenticationTicketFactory)
                defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
            var token = (CasSimpleMultifactorAuthenticationTicket) mfaFactory.create(tokenId, service, Map.of());
            assertThrows(FailedLoginException.class, () -> multifactorAuthenticationService.store(token));
        }
    }

    @Test
    public void verifyValidateTokenFails() throws Exception {
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser");
        try (val webServer = new MockWebServer(9229,
            new ByteArrayResource(MAPPER.writeValueAsString(authentication.getPrincipal())
                .getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            val credential = new CasSimpleMultifactorTokenCredential(UUID.randomUUID().toString());
            assertThrows(FailedLoginException.class, () -> multifactorAuthenticationService.validate(authentication.getPrincipal(), credential));
        }
    }

    @Test
    public void verifyValidateTokenOK() throws Exception {
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser");
        try (val webServer = new MockWebServer(9229,
            new ByteArrayResource(MAPPER.writeValueAsString(authentication.getPrincipal())
                .getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val credential = new CasSimpleMultifactorTokenCredential(UUID.randomUUID().toString());
            assertNotNull(multifactorAuthenticationService.validate(authentication.getPrincipal(), credential));
        }
    }
}
