package org.apereo.cas.syncope.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.syncope.BaseSyncopeTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.beans.BeanContainer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SyncopeAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Tag("Syncope")
@ExtendWith(CasTestExtension.class)
class SyncopeAuthenticationHandlerTests extends BaseSyncopeTests {
    private static final Credential CREDENTIAL =
        CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "password");

    @Nested
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @EnabledIfListeningOnPort(port = 18080)
    @SpringBootTest(classes = BaseSyncopeTests.SharedTestConfiguration.class,
        properties = "cas.authn.syncope.url=http://localhost:18080/syncope")
    class SyncopeCoreServerTests {
        @Autowired
        @Qualifier("syncopeAuthenticationHandlers")
        private BeanContainer<AuthenticationHandler> syncopeAuthenticationHandlers;

        @Test
        void verifyHandlerPasses() throws Throwable {
            assertNotNull(syncopeAuthenticationHandlers);
            val syncopeAuthenticationHandler = syncopeAuthenticationHandlers.first();
            val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("syncopecas", "Mellon");
            val result = syncopeAuthenticationHandler.authenticate(credential, mock(Service.class));
            assertNotNull(result);
        }

        @Test
        void verifyAccountMustChangePassword() {
            val syncopeAuthenticationHandler = syncopeAuthenticationHandlers.first();
            val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("syncopepasschange", "Sync0pe");
            assertThrows(AccountPasswordMustChangeException.class, () -> syncopeAuthenticationHandler.authenticate(credential, mock(Service.class)));
        }
    }

    @Nested
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @SpringBootTest(classes = BaseSyncopeTests.SharedTestConfiguration.class,
        properties = "cas.authn.syncope.url=http://localhost:8096")
    @Execution(ExecutionMode.SAME_THREAD)
    class SyncopeMockDataTests {

        @Autowired
        @Qualifier("syncopeAuthenticationHandlers")
        private BeanContainer<AuthenticationHandler> syncopeAuthenticationHandlers;

        @Test
        void verifyHandlerPasses() throws Throwable {
            val syncopeAuthenticationHandler = syncopeAuthenticationHandlers.first();
            try (val webserver = startMockSever(user(), HttpStatus.OK, 8096)) {
                assertDoesNotThrow(() ->
                    syncopeAuthenticationHandler.authenticate(CREDENTIAL, mock(Service.class)));
            }
        }

        @Test
        void verifyHandlerMustChangePassword() throws Throwable {
            val user = MAPPER.createObjectNode();
            user.put("username", "casuser");
            user.put("mustChangePassword", true);
            try (val webserver = startMockSever(user, HttpStatus.OK, 8096)) {
                val syncopeAuthenticationHandler = syncopeAuthenticationHandlers.first();
                assertThrows(AccountPasswordMustChangeException.class,
                    () -> syncopeAuthenticationHandler.authenticate(CREDENTIAL, mock(Service.class)));
            }
        }

        @Test
        void verifyHandlerSuspended() throws Throwable {
            val user = MAPPER.createObjectNode();
            user.put("username", "casuser");
            user.put("suspended", true);
            try (val webserver = startMockSever(user, HttpStatus.OK, 8096)) {
                val syncopeAuthenticationHandler = syncopeAuthenticationHandlers.first();
                assertThrows(AccountDisabledException.class,
                    () -> syncopeAuthenticationHandler.authenticate(CREDENTIAL, mock(Service.class)));
            }
        }

        @Test
        void verifyMembershipInfoPassed() throws Throwable {
            val syncopeAuthenticationHandler = syncopeAuthenticationHandlers.first();
            try (val webserver = startMockSever(userForMembershipsTypeExtension(), HttpStatus.OK, 8096)) {
                assertDoesNotThrow(() ->
                    syncopeAuthenticationHandler.authenticate(CREDENTIAL, mock(Service.class)));
                val result = syncopeAuthenticationHandler.authenticate(CREDENTIAL, mock(Service.class));
                assertNotNull(result);
                assertFalse(result.getPrincipal().getAttributes().get("syncopeUserMemberships").isEmpty());
                assertEquals(1, result.getPrincipal().getAttributes().get("syncopeUserMemberships").size());
                Map<String, String> membershipAttrs =
                    (Map<String, String>) result.getPrincipal().getAttributes().get("syncopeUserMemberships").get(0);
                assertEquals(3, membershipAttrs.size());
                assertTrue(membershipAttrs.containsKey("groupName"));
                assertTrue(membershipAttrs.containsKey("testSchema1"));
                assertTrue(membershipAttrs.containsKey("testSchema2"));
                assertEquals("G1", membershipAttrs.get("groupName"));
                assertEquals("[\"valueSchema1\"]", membershipAttrs.get("testSchema1"));
                assertEquals("[\"valueSchema2\"]", membershipAttrs.get("testSchema2"));
            }
        }
    }
}
