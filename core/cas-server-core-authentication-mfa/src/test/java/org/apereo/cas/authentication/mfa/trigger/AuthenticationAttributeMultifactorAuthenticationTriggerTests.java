package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationAttributeMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("MFATrigger")
class AuthenticationAttributeMultifactorAuthenticationTriggerTests {
    @Nested
    @TestPropertySource(properties = {
        "cas.authn.mfa.triggers.authentication.global-authentication-attribute-name-triggers=category",
        "cas.authn.mfa.triggers.authentication.global-authentication-attribute-value-regex=.+object.*"
    })
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    class DefaultTests extends BaseMultifactorAuthenticationTriggerTests {

        @Test
        void verifyOperationByProvider() throws Throwable {
            val authentication = CoreAuthenticationTestUtils.getAuthentication(
                "casuser", Map.of("category", List.of("user-object-class", "another")));

            val result = authenticationAttributeMultifactorAuthenticationTrigger.isActivated(authentication,
                CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), mock(Service.class));
            assertTrue(result.isPresent());
        }

        @Test
        void verifyNoMatch() throws Throwable {
            val authentication = CoreAuthenticationTestUtils.getAuthentication(
                "casuser", Map.of("whatever", List.of("something", "whatever")));
            val result = authenticationAttributeMultifactorAuthenticationTrigger.isActivated(authentication,
                CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), mock(Service.class));
            assertTrue(result.isEmpty());
        }
    }


    @Nested
    @TestPropertySource(properties = {
        "cas.authn.mfa.triggers.authentication.global-authentication-attribute-name-triggers=mfa-mode",
        "cas.authn.mfa.triggers.authentication.global-authentication-attribute-value-regex=mfa-other"
    })
    @Import({
        MultipleProvidersTests.TestMultifactorTestConfiguration.class,
        BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class
    })
    class MultipleProvidersTests extends BaseMultifactorAuthenticationTriggerTests {

        @TestConfiguration(value = "TestMultifactorTestConfiguration", proxyBeanMethods = false)
        static class TestMultifactorTestConfiguration {
            @Bean
            public MultifactorAuthenticationProvider dummyProvider() {
                return new TestMultifactorAuthenticationProvider("mfa-other");
            }
        }

        @Test
        void verifyMultipleProvider() throws Throwable {
            val authentication = CoreAuthenticationTestUtils.getAuthentication(
                "casuser", Map.of("mfa-mode", List.of("mfa-other")));
            val result = authenticationAttributeMultifactorAuthenticationTrigger.isActivated(authentication,
                CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), mock(Service.class));
            assertTrue(result.isPresent());
        }
    }

}
