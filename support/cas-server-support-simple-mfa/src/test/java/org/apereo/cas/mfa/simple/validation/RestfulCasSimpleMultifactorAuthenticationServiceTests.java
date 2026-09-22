package org.apereo.cas.mfa.simple.validation;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulCasSimpleMultifactorAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.simple.token.rest.url=https://localhost/mfa-token",
        "cas.authn.mfa.simple.token.rest.headers.h1=h2"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
class RestfulCasSimpleMultifactorAuthenticationServiceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleValueAsArray(true).defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier(CasSimpleMultifactorAuthenticationService.BEAN_NAME)
    private CasSimpleMultifactorAuthenticationService multifactorAuthenticationService;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    private TicketFactory defaultTicketFactory;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyGenerateToken() throws Throwable {
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser");
        val tokenId = UUID.randomUUID().toString();
        val service = RegisteredServiceTestUtils.getService();
        try (val webServer = new MockWebServer(
            new ByteArrayResource(tokenId.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val token = serviceFor(webServer).generate(authentication.getPrincipal(), service);
            assertEquals(token.getId(), tokenId);
            assertEquals(token.getService(), service);
        }

        try (val webServer = new MockWebServer(
            new ByteArrayResource(tokenId.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            assertThrows(FailedLoginException.class, () -> serviceFor(webServer).generate(authentication.getPrincipal(), service));
        }
    }

    @Test
    void verifyStoreToken() {
        val tokenId = UUID.randomUUID().toString();
        val service = RegisteredServiceTestUtils.getService();
        try (val webServer = new MockWebServer(
            new ByteArrayResource(tokenId.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.CREATED)) {
            webServer.start();
            val mfaFactory = (CasSimpleMultifactorAuthenticationTicketFactory)
                defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
            var token = mfaFactory.create(tokenId, service, Map.of());
            assertDoesNotThrow(() -> serviceFor(webServer).store(token));
        }
        try (val webServer = new MockWebServer(
            new ByteArrayResource(tokenId.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            val mfaFactory = (CasSimpleMultifactorAuthenticationTicketFactory)
                defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
            var token = mfaFactory.create(tokenId, service, Map.of());
            assertThrows(FailedLoginException.class, () -> serviceFor(webServer).store(token));
        }
    }

    @Test
    void verifyValidateTokenFails() {
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser");
        try (val webServer = new MockWebServer(
            new ByteArrayResource(MAPPER.writeValueAsString(authentication.getPrincipal())
                .getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            val credential = new CasSimpleMultifactorTokenCredential(UUID.randomUUID().toString());
            assertThrows(FailedLoginException.class, () -> serviceFor(webServer).validate(authentication.getPrincipal(), credential));
        }
    }

    @Test
    void verifyValidateTokenOK() throws Throwable {
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser");
        try (val webServer = new MockWebServer(
            new ByteArrayResource(MAPPER.writeValueAsString(authentication.getPrincipal())
                .getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val credential = new CasSimpleMultifactorTokenCredential(UUID.randomUUID().toString());
            assertNotNull(serviceFor(webServer).validate(authentication.getPrincipal(), credential));
        }
    }

    @Test
    void verifyFetchTokenOK() throws Throwable {
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser");
        try (val webServer = new MockWebServer(
            new ByteArrayResource(MAPPER.writeValueAsString(authentication.getPrincipal())
                .getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val credential = new CasSimpleMultifactorTokenCredential(UUID.randomUUID().toString());
            assertNotNull(serviceFor(webServer).fetch(credential));
        }
    }

    @Test
    void verifyUpdatePrincipal() {
        try (val webServer = new MockWebServer(HttpStatus.OK)) {
            webServer.start();
            val attributes = CollectionUtils.<String, Object>wrap("email", "casuser@example.org");
            assertDoesNotThrow(() -> serviceFor(webServer).update(RegisteredServiceTestUtils.getPrincipal(), attributes));
        }
    }

    @Test
    void verifyServiceIsResolvedFromConfiguration() {
        assertInstanceOf(RestfulCasSimpleMultifactorAuthenticationService.class, multifactorAuthenticationService);
    }

    private RestfulCasSimpleMultifactorAuthenticationService serviceFor(final MockWebServer webServer) {
        val properties = new CasConfigurationProperties().getAuthn().getMfa().getSimple().getToken().getRest();
        properties.setUrl("http://localhost:%s".formatted(webServer.getPort()));
        properties.getHeaders().put("h1", "h2");
        return new RestfulCasSimpleMultifactorAuthenticationService(ticketRegistry, properties, defaultTicketFactory);
    }
}
