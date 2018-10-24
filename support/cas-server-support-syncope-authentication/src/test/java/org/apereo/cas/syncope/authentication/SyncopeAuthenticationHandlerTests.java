package org.apereo.cas.syncope.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.SyncopeAuthenticationConfiguration;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.syncope.common.lib.to.UserTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link SyncopeAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    SyncopeAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasPersonDirectoryTestConfiguration.class
})
@TestPropertySource(properties = "cas.authn.syncope.url=http://localhost:8095")
@Slf4j
public class SyncopeAuthenticationHandlerTests {
    private static final ObjectMapper MAPPER = new IgnoringJaxbModuleJacksonObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("syncopeAuthenticationHandler")
    private AuthenticationHandler syncopeAuthenticationHandler;

    private MockWebServer webServer;

    @Test
    public void verifyHandlerPasses() {
        try {
            val user = new UserTO();
            user.setUsername("casuser");
            startMockSever(user);
            syncopeAuthenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        } finally {
            this.webServer.stop();
        }
    }

    @Test
    public void verifyHandlerMustChangePassword() {
        try {
            val user = new UserTO();
            user.setUsername("casuser");
            user.setMustChangePassword(true);
            startMockSever(user);

            syncopeAuthenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        } catch (final AccountPasswordMustChangeException e) {
            LOGGER.debug("Passed");
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        } finally {
            this.webServer.stop();
        }
    }

    @Test
    public void verifyHandlerSuspended() {
        try {
            val user = new UserTO();
            user.setUsername("casuser");
            user.setSuspended(true);
            startMockSever(user);

            syncopeAuthenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        } catch (final AccountDisabledException e) {
            LOGGER.debug("Passed");
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        } finally {
            this.webServer.stop();
        }
    }

    private void startMockSever(final UserTO user) throws JsonProcessingException {
        val data = MAPPER.writeValueAsString(user);
        this.webServer = new MockWebServer(8095,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        this.webServer.start();
    }
}
