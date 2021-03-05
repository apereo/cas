package org.apereo.cas.adaptors.rest;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRestAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * This is {@link RestAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasRestAuthenticationConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreServicesConfiguration.class,
    RefreshAutoConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreConfiguration.class
},
    properties = "cas.authn.rest.uri=http://localhost:8081/authn")
@EnableScheduling
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Tag("RestfulApi")
public class RestAuthenticationHandlerTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("restAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("restAuthenticationTemplate")
    private RestTemplate restAuthenticationTemplate;

    private ResponseActions server;

    @BeforeEach
    public void initialize() {
        server = MockRestServiceServer.bindTo(restAuthenticationTemplate).build()
            .expect(manyTimes(), requestTo("http://localhost:8081/authn"))
            .andExpect(method(HttpMethod.POST));
    }

    @Test
    public void verifySuccess() throws Exception {
        val principalWritten = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casuser");
        val writer = new StringWriter();
        MAPPER.writeValue(writer, principalWritten);

        val headers = new HttpHeaders();

        val instant = Instant.now(Clock.systemUTC()).plus(10, ChronoUnit.DAYS);
        val formatted = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneOffset.UTC)
            .format(instant);

        headers.add(RestAuthenticationHandler.HEADER_NAME_CAS_PASSWORD_EXPIRATION_DATE, formatted);
        headers.add(RestAuthenticationHandler.HEADER_NAME_CAS_WARNING, "warning1");
        headers.add(RestAuthenticationHandler.HEADER_NAME_CAS_WARNING, "warning2");
        server.andRespond(withSuccess(writer.toString(), MediaType.APPLICATION_JSON).headers(headers));
        val res = authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("casuser", res.getPrincipal().getId());
    }

    @Test
    public void verifyNoPrincipal() {
        server.andRespond(withSuccess(StringUtils.EMPTY, MediaType.APPLICATION_JSON));
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifySuccessRaw() throws Exception {
        val response = IOUtils.toString(new ClassPathResource("rest-authn-response.json").getInputStream(), StandardCharsets.UTF_8);
        server.andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        val res = authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("casuser", res.getPrincipal().getId());
        val attributes = res.getPrincipal().getAttributes();
        assertEquals(6, attributes.size());
        assertTrue(attributes.containsKey("mail"));
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("displayName"));
        assertTrue(attributes.containsKey("eduPersonAffiliation"));
    }

    @Test
    public void verifyDisabledAccount() {
        server.andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON));
        assertThrows(AccountDisabledException.class,
            () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyUnauthorized() {
        server.andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyOther() {
        server.andRespond(withStatus(HttpStatus.REQUEST_TIMEOUT));
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyLocked() {
        server.andRespond(withStatus(HttpStatus.LOCKED));
        assertThrows(AccountLockedException.class,
            () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyConditionReq() {
        server.andRespond(withStatus(HttpStatus.PRECONDITION_REQUIRED));
        assertThrows(AccountPasswordMustChangeException.class,
            () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyConditionFail() {
        server.andRespond(withStatus(HttpStatus.PRECONDITION_FAILED));
        assertThrows(AccountExpiredException.class,
            () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyNotFound() {
        server.andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(AccountNotFoundException.class,
            () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }
}



