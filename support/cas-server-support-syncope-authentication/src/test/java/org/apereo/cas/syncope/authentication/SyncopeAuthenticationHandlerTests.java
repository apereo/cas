package org.apereo.cas.syncope.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.SyncopeAuthenticationConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.Cleanup;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SyncopeAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SuppressWarnings("unused")
@SpringBootTest(classes = {
    SyncopeAuthenticationConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    BaseSyncopeTests.SharedTestConfiguration.class
}, properties = "cas.authn.syncope.url=http://localhost:8095")
@ResourceLock("Syncope")
@Tag("AuthenticationHandler")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SyncopeAuthenticationHandlerTests extends BaseSyncopeTests {

    @Autowired
    @Qualifier("syncopeAuthenticationHandlers")
    private BeanContainer<AuthenticationHandler> syncopeAuthenticationHandlers;

    @Test
    public void verifyHandlerPasses() {
        val user = user();
        val syncopeAuthenticationHandler = syncopeAuthenticationHandlers.first();

        @Cleanup("stop")
        val webserver = startMockSever(user);
        assertDoesNotThrow(() -> syncopeAuthenticationHandler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "password")));
    }

    @Test
    public void verifyHandlerMustChangePassword() {
        val user = MAPPER.createObjectNode();
        user.put("username", "casuser");
        user.put("mustChangePassword", true);
        @Cleanup("stop")
        val webserver = startMockSever(user);

        val syncopeAuthenticationHandler = syncopeAuthenticationHandlers.first();
        assertThrows(AccountPasswordMustChangeException.class,
                () -> syncopeAuthenticationHandler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "password")));
    }

    @Test
    public void verifyHandlerSuspended() {
        val user = MAPPER.createObjectNode();
        user.put("username", "casuser");
        user.put("suspended", true);
        @Cleanup("stop")
        val webserver = startMockSever(user);

        val syncopeAuthenticationHandler = syncopeAuthenticationHandlers.first();
        assertThrows(AccountDisabledException.class,
                () -> syncopeAuthenticationHandler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "password")));
    }
}
