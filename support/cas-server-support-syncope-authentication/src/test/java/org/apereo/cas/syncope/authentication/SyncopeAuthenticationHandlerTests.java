package org.apereo.cas.syncope.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.SyncopeAuthenticationConfiguration;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.syncope.common.lib.to.UserTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SyncopeAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SuppressWarnings("unused")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    SyncopeAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasPersonDirectoryTestConfiguration.class
}, properties = {
    "cas.authn.syncope.url=http://localhost:8095",
    "spring.mail.host=localhost",
    "spring.mail.port=25000",
    "spring.mail.testConnection=false"
})
@Slf4j
@ResourceLock("Syncope")
public class SyncopeAuthenticationHandlerTests {
    private static final ObjectMapper MAPPER = new IgnoringJaxbModuleJacksonObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("syncopeAuthenticationHandler")
    private AuthenticationHandler syncopeAuthenticationHandler;

    @SneakyThrows
    private static MockWebServer startMockSever(final UserTO user) {
        val data = MAPPER.writeValueAsString(user);
        val webServer = new MockWebServer(8095,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        webServer.start();
        return webServer;
    }

    @Test
    public void verifyHandlerPasses() {
        val user = new UserTO();
        user.setUsername("casuser");
        @Cleanup("stop")
        val webserver = startMockSever(user);
        assertDoesNotThrow(() ->
            syncopeAuthenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser")));
    }

    @Test
    public void verifyHandlerMustChangePassword() {
        val user = new UserTO();
        user.setUsername("casuser");
        user.setMustChangePassword(true);
        @Cleanup("stop")
        val webserver = startMockSever(user);

        assertThrows(AccountPasswordMustChangeException.class,
            () -> syncopeAuthenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser")));
    }

    @Test
    public void verifyHandlerSuspended() {
        val user = new UserTO();
        user.setUsername("casuser");
        user.setSuspended(true);
        @Cleanup("stop")
        val webserver = startMockSever(user);

        assertThrows(AccountDisabledException.class,
            () -> syncopeAuthenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser")));
    }
}
