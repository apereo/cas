package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
class RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests {

    @Nested
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    class DefaultTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyOperationByProvider() throws Throwable {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getPrincipalAttributeNameTrigger()).thenReturn("mail");
            when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
            when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();

            assertTrue(registeredServicePrincipalAttributeMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), null,
                new MockHttpServletRequest(), new MockHttpServletResponse(),
                CoreAuthenticationTestUtils.getService()).isEmpty());

            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(null);
            assertTrue(registeredServicePrincipalAttributeMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                new MockHttpServletRequest(), new MockHttpServletResponse(),
                CoreAuthenticationTestUtils.getService()).isEmpty());


            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            val result = registeredServicePrincipalAttributeMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                new MockHttpServletRequest(), new MockHttpServletResponse(),
                CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());
        }

        @Test
        void verifyMismatchAttributes() throws Throwable {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getPrincipalAttributeNameTrigger()).thenReturn("bad-attribute");
            when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
            when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

            val result = registeredServicePrincipalAttributeMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(),
                registeredService, new MockHttpServletRequest(), new MockHttpServletResponse(),
                CoreAuthenticationTestUtils.getService());
            assertFalse(result.isPresent());
        }

        @Test
        void verifyPolicyNoAttributes() throws Throwable {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getPrincipalAttributeNameTrigger()).thenReturn("email");
            when(policy.getPrincipalAttributeValueToMatch()).thenReturn(StringUtils.EMPTY);
            when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            val result = registeredServicePrincipalAttributeMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(),
                registeredService, new MockHttpServletRequest(), new MockHttpServletResponse(),
                CoreAuthenticationTestUtils.getService());
            assertTrue(result.isEmpty());
        }
    }
    
    @Nested
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    @TestPropertySource(properties = "cas.authn.mfa.triggers.principal.deny-if-unmatched=true")
    class MismatchDeniedTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyMismatchAttributesMustDeny() {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getPrincipalAttributeNameTrigger()).thenReturn("bad-attribute");
            when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
            when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            assertThrows(AuthenticationException.class,
                () -> registeredServicePrincipalAttributeMultifactorAuthenticationTrigger.isActivated(
                    CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                    new MockHttpServletRequest(), new MockHttpServletResponse(),
                    CoreAuthenticationTestUtils.getService()));
        }
    }

    @Nested
    @Import({
        BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class,
        RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests.SecondMultifactorTestConfiguration.class
    })
    @TestPropertySource(properties = "cas.authn.mfa.core.provider-selection.provider-selection-enabled=true")
    class CompositeTests extends BaseMultifactorAuthenticationTriggerTests {
        @Autowired
        @Qualifier("simpleProvider")
        private MultifactorAuthenticationProvider simpleProvider;

        @Test
        void verifyOperationByCompositeProvider() throws Throwable {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getPrincipalAttributeNameTrigger()).thenReturn("mail");
            when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
            when(policy.getMultifactorAuthenticationProviders()).thenReturn(
                Set.of(TestMultifactorAuthenticationProvider.ID, simpleProvider.getId()));
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            val result = registeredServicePrincipalAttributeMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                new MockHttpServletRequest(), new MockHttpServletResponse(),
                CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());
            assertInstanceOf(ChainingMultifactorAuthenticationProvider.class, result.get());
            assertEquals(Ordered.LOWEST_PRECEDENCE, registeredServicePrincipalAttributeMultifactorAuthenticationTrigger.getOrder());
        }
    }

    @Nested
    @Import({
        RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests.SecondMultifactorTestConfiguration.class,
        BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class
    })
    class MultipleProvidersTests extends BaseMultifactorAuthenticationTriggerTests {
        @Autowired
        @Qualifier("simpleProvider")
        private MultifactorAuthenticationProvider simpleProvider;

        @Test
        void verifyOperationByMultipleProviders() throws Throwable {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getPrincipalAttributeNameTrigger()).thenReturn("mail");
            when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
            when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(
                TestMultifactorAuthenticationProvider.ID, simpleProvider.getId()));
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            val result = registeredServicePrincipalAttributeMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                new MockHttpServletRequest(), new MockHttpServletResponse(),
                CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());
            assertEquals(simpleProvider.getId(), result.get().getId());
        }
    }

    @TestConfiguration(value = "SecondMultifactorTestConfiguration", proxyBeanMethods = false)
    static class SecondMultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider simpleProvider() {
            return new TestMultifactorAuthenticationProvider("mfa-simple");
        }
    }

}
