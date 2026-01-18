package org.apereo.cas.pm;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.pm.web.flow.actions.BasePasswordManagementActionTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link RestfulPasswordSynchronizationAuthenticationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@ExtendWith(CasTestExtension.class)
@Tag("RestfulApi")
class RestfulPasswordSynchronizationAuthenticationPostProcessorTests {

    @TestPropertySource(properties = {
        "cas.authn.password-sync.enabled=true",
        "cas.authn.password-sync.rest.url=http://localhost:${random.int[3000,9999]}"
    })
    @Import(RestfulPasswordSynchronizationAuthenticationPostProcessorTests.AuthenticationTestConfiguration.class)
    abstract static class BasePostProcessorTests extends BasePasswordManagementActionTests {
        @Autowired
        @Qualifier(AuthenticationManager.BEAN_NAME)
        protected AuthenticationManager authenticationManager;
    }

    @Nested
    class SyncTests extends BasePostProcessorTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = URI.create(casProperties.getAuthn().getPasswordSync().getRest().getUrl()).getPort();
            try (val webServer = new MockWebServer(port, HttpStatus.OK)) {
                webServer.start();
                authenticationManager.authenticate(CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
                    .newTransaction(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
            }
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.password-sync.rest.asynchronous=true")
    class AsyncTests extends BasePostProcessorTests {
        @Test
        void verifyOperation() throws Throwable {
            val port = URI.create(casProperties.getAuthn().getPasswordSync().getRest().getUrl()).getPort();
            try (val webServer = new MockWebServer(port, HttpStatus.OK)) {
                webServer.start();
                authenticationManager.authenticate(CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
                    .newTransaction(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
            }
        }
    }

    @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationTestConfiguration {
        @Bean
        public AuthenticationEventExecutionPlanConfigurer testAuthenticationEventExecutionPlanConfigurer(
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final ObjectProvider<PrincipalResolver> defaultPrincipalResolver) {
            return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(
                new SimpleTestUsernamePasswordAuthenticationHandler(), defaultPrincipalResolver.getObject());
        }
    }
}
