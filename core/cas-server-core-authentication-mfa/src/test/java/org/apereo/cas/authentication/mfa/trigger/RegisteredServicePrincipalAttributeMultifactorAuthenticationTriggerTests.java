package org.apereo.cas.authentication.mfa.trigger;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredService;
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
            when(policy.getMultifactorAuthenticationProviders()).thenReturn(new LinkedHashSet<>(List.of(
                TestMultifactorAuthenticationProvider.ID, simpleProvider.getId())));
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

    @Nested
    @Import({
        RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests.SecondMultifactorTestConfiguration.class,
        BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class
    })
    class MappedProvidersTests extends BaseMultifactorAuthenticationTriggerTests {
        @Autowired
        @Qualifier("registeredServiceMappedPrincipalAttributeToMultifactorAuthenticationTrigger")
        private MultifactorAuthenticationTrigger mappedTrigger;

        @Autowired
        @Qualifier("simpleProvider")
        private MultifactorAuthenticationProvider simpleProvider;

        private Optional<MultifactorAuthenticationProvider> activate(final RegisteredService registeredService) throws Throwable {
            return activate(CoreAuthenticationTestUtils.getAuthentication(), registeredService);
        }

        private Optional<MultifactorAuthenticationProvider> activate(final Authentication authentication,
                                                                     final RegisteredService registeredService)
                throws Throwable {
            return mappedTrigger.isActivated(authentication, registeredService,
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
        }

        @Test
        void verifyResolverIsRegistered() {
            assertTrue(applicationContext.containsBean("registeredServiceMappedPrincipalAttributeMultifactorWebflowEventResolver"));
            assertEquals(Ordered.LOWEST_PRECEDENCE, mappedTrigger.getOrder());
        }

        @Test
        void verifyMissingAuthenticationOrService() throws Throwable {
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            assertTrue(activate(null, registeredService).isEmpty());
            assertTrue(activate(CoreAuthenticationTestUtils.getAuthentication(), null).isEmpty());
        }

        @Test
        void verifyPolicyNotEligible() throws Throwable {
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(null);
            assertTrue(activate(registeredService).isEmpty());

            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getPrincipalAttributeNameTrigger()).thenReturn("mail");
            when(policy.getMultifactorProviderSelectionMappings()).thenReturn(null);
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            assertTrue(activate(registeredService).isEmpty());

            val noMappings = new DefaultRegisteredServiceMultifactorPolicy().setPrincipalAttributeNameTrigger("mail");
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(noMappings);
            assertTrue(activate(registeredService).isEmpty());

            val noAttribute = new DefaultRegisteredServiceMultifactorPolicy()
                .setMultifactorProviderSelectionMappings(mappings(TestMultifactorAuthenticationProvider.ID, ".+"));
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(noAttribute);
            assertTrue(activate(registeredService).isEmpty());

            val explicitProviders = new DefaultRegisteredServiceMultifactorPolicy()
                .setPrincipalAttributeNameTrigger("mail")
                .setMultifactorAuthenticationProviders(Set.of(TestMultifactorAuthenticationProvider.ID))
                .setMultifactorProviderSelectionMappings(mappings(TestMultifactorAuthenticationProvider.ID, ".+"));
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(explicitProviders);
            assertTrue(activate(registeredService).isEmpty());
        }

        @Test
        void verifySelectsMatchingProvider() throws Throwable {
            val registeredService = serviceWithMappings("mail",
                mappings(TestMultifactorAuthenticationProvider.ID, "(?i)^nobody@.+$", simpleProvider.getId(), "(?i).+@example\\.org$"));
            val result = activate(registeredService);
            assertTrue(result.isPresent());
            assertEquals(simpleProvider.getId(), result.get().getId());
        }

        @Test
        void verifyFirstMatchingMappingWins() throws Throwable {
            var registeredService = serviceWithMappings("mail",
                mappings(simpleProvider.getId(), ".+@example.*", TestMultifactorAuthenticationProvider.ID, ".+@example.*"));
            assertEquals(simpleProvider.getId(), activate(registeredService).orElseThrow().getId());

            registeredService = serviceWithMappings("mail",
                mappings(TestMultifactorAuthenticationProvider.ID, ".+@example.*", simpleProvider.getId(), ".+@example.*"));
            assertEquals(TestMultifactorAuthenticationProvider.ID, activate(registeredService).orElseThrow().getId());
        }

        @Test
        void verifyMultivaluedAttributeAndAttributeNames() throws Throwable {
            val registeredService = serviceWithMappings("unknown-attribute,memberOf",
                mappings(TestMultifactorAuthenticationProvider.ID, "^faculty$", simpleProvider.getId(), "^staff$"));
            assertEquals(simpleProvider.getId(), activate(registeredService).orElseThrow().getId());
        }

        @Test
        void verifyUnknownProviderIsSkipped() throws Throwable {
            val registeredService = serviceWithMappings("mail",
                mappings("mfa-unknown", ".+", TestMultifactorAuthenticationProvider.ID, ".+"));
            assertEquals(TestMultifactorAuthenticationProvider.ID, activate(registeredService).orElseThrow().getId());

            val unknownOnly = serviceWithMappings("mail", mappings("mfa-unknown", ".+"));
            assertTrue(activate(unknownOnly).isEmpty());
        }

        @Test
        void verifyNoMatch() throws Throwable {
            val missingValue = new LinkedHashMap<String, Serializable>();
            missingValue.put(TestMultifactorAuthenticationProvider.ID, null);
            missingValue.put(simpleProvider.getId(), "^nomatch$");
            assertTrue(activate(serviceWithMappings("mail", missingValue)).isEmpty());
            assertTrue(activate(serviceWithMappings("bad-attribute", mappings(simpleProvider.getId(), ".+"))).isEmpty());
        }
    }

    @Nested
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    @TestPropertySource(properties = "cas.authn.mfa.triggers.principal.deny-if-unmatched=true")
    class MappedProvidersMismatchDeniedTests extends BaseMultifactorAuthenticationTriggerTests {
        @Autowired
        @Qualifier("registeredServiceMappedPrincipalAttributeToMultifactorAuthenticationTrigger")
        private MultifactorAuthenticationTrigger mappedTrigger;

        @Test
        void verifyMismatchMustDeny() {
            val registeredService = serviceWithMappings("mail", mappings(TestMultifactorAuthenticationProvider.ID, "^nomatch$"));
            assertThrows(AuthenticationException.class, () -> mappedTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService()));
        }

        @Test
        void verifyMatchIsAllowed() throws Throwable {
            val registeredService = serviceWithMappings("mail", mappings(TestMultifactorAuthenticationProvider.ID, ".+@example.*"));
            val result = mappedTrigger.isActivated(CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertEquals(TestMultifactorAuthenticationProvider.ID, result.orElseThrow().getId());
        }
    }

    private static Map<String, Serializable> mappings(final String... entries) {
        val mappings = new LinkedHashMap<String, Serializable>();
        for (var i = 0; i < entries.length; i += 2) {
            mappings.put(entries[i], entries[i + 1]);
        }
        return mappings;
    }

    private static RegisteredService serviceWithMappings(final String attributeName, final Map<String, Serializable> mappings) {
        val policy = new DefaultRegisteredServiceMultifactorPolicy()
            .setPrincipalAttributeNameTrigger(attributeName)
            .setMultifactorProviderSelectionMappings(mappings);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        return registeredService;
    }

    @TestConfiguration(value = "SecondMultifactorTestConfiguration", proxyBeanMethods = false)
    static class SecondMultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider simpleProvider() {
            return new TestMultifactorAuthenticationProvider("mfa-simple");
        }
    }

}
