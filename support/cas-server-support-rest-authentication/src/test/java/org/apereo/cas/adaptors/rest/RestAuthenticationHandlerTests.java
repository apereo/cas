package org.apereo.cas.adaptors.rest;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.IOUtils;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

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
    CasCoreConfiguration.class
},
    properties = "cas.authn.rest.uri=http://localhost:8081/authn")
@EnableScheduling
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Tag("RestfulApi")
public class RestAuthenticationHandlerTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

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
        server.andRespond(withSuccess(writer.toString(), MediaType.APPLICATION_JSON));
        val res = authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("casuser", res.getPrincipal().getId());
    }

    @Test
    public void verifySuccessRaw() throws Exception {
        val response = IOUtils.toString(new ClassPathResource("rest-authn-response.json").getInputStream(), StandardCharsets.UTF_8);
        server.andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        val res = authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("casuser", res.getPrincipal().getId());
        assertEquals(6, res.getPrincipal().getAttributes().size());
        assertTrue(res.getPrincipal().getAttributes().containsKey("mail"));
        assertTrue(res.getPrincipal().getAttributes().containsKey("givenName"));
        assertTrue(res.getPrincipal().getAttributes().containsKey("displayName"));
        assertTrue(res.getPrincipal().getAttributes().containsKey("eduPersonAffiliation"));
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
    public void verifyNotFound() {
        server.andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(AccountNotFoundException.class,
            () -> authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }
}



