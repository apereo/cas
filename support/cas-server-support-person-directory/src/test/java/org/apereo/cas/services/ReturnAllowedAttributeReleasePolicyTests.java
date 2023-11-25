package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("Attributes")
@Execution(ExecutionMode.SAME_THREAD)
class ReturnAllowedAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnAllowedAttributeReleasePolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @SpringBootTest(classes = {
        CasPersonDirectoryTestConfiguration.class,
        ReturnAllowedAttributeReleasePolicyTestConfiguration.class,
        CasCoreUtilConfiguration.class,
        RefreshAutoConfiguration.class
    },
        properties = "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/return-allowed-definitions.json")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Nested
    class AttributeDefinitionsTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyUnresolvedAttributes() throws Throwable {
            val policy = new ReturnAllowedAttributeReleasePolicy();
            policy.setAllowedAttributes(CollectionUtils.wrapList("displayName"));

            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
                .applicationContext(applicationContext)
                .service(CoreAuthenticationTestUtils.getService())
                .principal(CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("casuser"))))
                .build();
            val attributes = policy.getAttributes(context);
            assertEquals(1, attributes.size());
            assertTrue(attributes.containsKey("displayName"));
        }

        @Test
        void verifyVirtualAttributesInChain() throws Throwable {
            val policy = new ReturnAllowedAttributeReleasePolicy();
            policy.setAllowedAttributes(CollectionUtils.wrapList("displayName"));
            policy.setOrder(0);

            val policy2 = new ReturnAllowedAttributeReleasePolicy();
            policy2.setAllowedAttributes(CollectionUtils.wrapList("calculated-displayName"));
            policy2.setOrder(1);

            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
                .applicationContext(applicationContext)
                .service(CoreAuthenticationTestUtils.getService())
                .principal(CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("casuser"))))
                .build();

            val chain = new ChainingAttributeReleasePolicy();
            chain.addPolicies(policy, policy2);

            val attributes = chain.getAttributes(context);
            assertEquals(2, attributes.size());
            assertTrue(attributes.containsKey("displayName"));
            assertTrue(attributes.containsKey("calculated-displayName"));
        }
    }

    @SpringBootTest(classes = {
        CasPersonDirectoryTestConfiguration.class,
        ReturnAllowedAttributeReleasePolicyTestConfiguration.class,
        CasCoreUtilConfiguration.class,
        RefreshAutoConfiguration.class
    },
        properties = {
            "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/return-allowed-definitions.json",
            "cas.authn.attribute-repository.core.default-attributes-to-release=cn,mail"
        })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    class DefaultTests {

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifySerialization() throws IOException {
            val allowedAttributes = new ArrayList<String>();
            allowedAttributes.add("attributeOne");
            val policyWritten = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
            policyWritten.setCanonicalizationMode(CaseCanonicalizationMode.UPPER.name());
            policyWritten.setPrincipalIdAttribute("principalId");
            MAPPER.writeValue(JSON_FILE, policyWritten);
            val policyRead = MAPPER.readValue(JSON_FILE, ReturnAllowedAttributeReleasePolicy.class);
            assertEquals(policyWritten, policyRead);
        }

        @Test
        void verifyInlineGroovy() throws Throwable {
            val allowedAttributes = new ArrayList<String>();
            allowedAttributes.add("uid");
            allowedAttributes.add("groovy { [ fullName: [ 'MyGivenName' + attributes['cn'][0] ] ] } ");
            for (int i = 0; i < 5; i++) {
                val policy = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
                val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
                val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
                when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);
                val context = RegisteredServiceAttributeReleasePolicyContext
                    .builder()
                    .applicationContext(applicationContext)
                    .registeredService(registeredService)
                    .service(CoreAuthenticationTestUtils.getService())
                    .principal(principal)
                    .build();
                val results = policy.getConsentableAttributes(context);
                assertTrue(results.containsKey("uid"));
                assertTrue(results.containsKey("fullName"));
            }
        }

        @Test
        void verifyConsentable() throws Throwable {
            val allowedAttributes = new ArrayList<String>();
            allowedAttributes.add("uid");
            allowedAttributes.add("cn");
            allowedAttributes.add("givenName");
            val policy = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
            val consentPolicy = new DefaultRegisteredServiceConsentPolicy(Set.of("cn"), Set.of("givenName"));
            consentPolicy.setStatus(TriStateBoolean.TRUE);
            policy.setConsentPolicy(consentPolicy);
            val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .registeredService(registeredService)
                .service(CoreAuthenticationTestUtils.getService())
                .principal(principal)
                .build();
            val results = policy.getConsentableAttributes(context);
            assertEquals(1, results.size());
            assertTrue(results.containsKey("givenName"));
        }

        @Test
        void verifyRequestedDefinitions() throws Throwable {
            val allowedAttributes = new ArrayList<String>();
            allowedAttributes.add("uid");
            allowedAttributes.add("cn");
            allowedAttributes.add("givenName");
            val policy = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);

            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
                .service(CoreAuthenticationTestUtils.getService())
                .principal(CoreAuthenticationTestUtils.getPrincipal())
                .build();
            assertTrue(policy.determineRequestedAttributeDefinitions(context).containsAll(policy.getAllowedAttributes()));
        }

        @Test
        void verifyRequestedDefinitionsWithExistingPrincipalAttribute() throws Throwable {
            val allowedAttributes = new ArrayList<String>();
            allowedAttributes.add("custom-name");
            val policy = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
            val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", Map.of("customName", List.of("CAS")));
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .registeredService(registeredService)
                .service(CoreAuthenticationTestUtils.getService())
                .principal(principal)
                .build();
            val attributes = policy.getAttributes(context);
            assertTrue(attributes.containsKey("custom-name"));
            assertEquals("CAS", attributes.get("custom-Name").getFirst());
        }

        @Test
        void verifyRequestedDefinitionsWithoutPrincipalAttribute() throws Throwable {
            val allowedAttributes = new ArrayList<String>();
            allowedAttributes.add("given-name");
            allowedAttributes.add("uid");
            val policy = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
            val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", Map.of("uid", List.of("UID")));
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .registeredService(registeredService)
                .service(CoreAuthenticationTestUtils.getService())
                .principal(principal)
                .build();
            val attributes = policy.getAttributes(context);
            assertEquals(2, attributes.size());
            assertTrue(attributes.containsKey("given-name"));
            assertEquals("hello", attributes.get("given-name").getFirst());
        }

        @Test
        void verifyDefaultAttributes() throws Throwable {
            val policy = new ReturnAllowedAttributeReleasePolicy();
            policy.setPrincipalIdAttribute("principalId");
            policy.postLoad();

            val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getUsernameAttributeProvider()).thenReturn(new RegisteredServiceUsernameAttributeProvider() {
                @Serial
                private static final long serialVersionUID = 6935950848419028873L;

                @Override
                public String resolveUsername(final RegisteredServiceUsernameProviderContext context) {
                    return context.getPrincipal().getId();
                }
            });

            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .registeredService(registeredService)
                .service(CoreAuthenticationTestUtils.getService())
                .principal(principal)
                .build();
            val attributes = policy.getAttributes(context);
            assertEquals(3, attributes.size());
            assertTrue(attributes.containsKey("principalId"));
            assertTrue(attributes.containsKey("cn"));
            assertTrue(attributes.containsKey("mail"));
        }

        @ParameterizedTest
        @MethodSource("getValueCaseTransformationTestParameters")
        void verifyValueCaseTransformation(final String canonicalizationMode, final List<String> expectedValues) throws Throwable {
            val policy = new ReturnAllowedAttributeReleasePolicy();
            policy.setCanonicalizationMode(canonicalizationMode);
            policy.setAllowedAttributes(List.of("uid"));
            policy.postLoad();
            val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", Map.of("uid", List.of("cas1", "cas2")));
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .registeredService(registeredService)
                .service(CoreAuthenticationTestUtils.getService())
                .principal(principal)
                .build();
            val attributes = policy.getAttributes(context);
            assertEquals(1, attributes.size());
            val values = attributes.get("uid");
            assertEquals(values, expectedValues);
        }

        public static Stream<Arguments> getValueCaseTransformationTestParameters() {
            return Stream.of(
                arguments(CaseCanonicalizationMode.LOWER.name(), List.of("cas1", "cas2")),
                arguments(CaseCanonicalizationMode.UPPER.name(), List.of("CAS1", "CAS2")),
                arguments(CaseCanonicalizationMode.NONE.name(), List.of("cas1", "cas2")),
                arguments(null, List.of("cas1", "cas2"))
            );
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ReturnAllowedAttributeReleasePolicyTestConfiguration {
        @Bean
        public ServicesManager servicesManager() {
            return mock(ServicesManager.class);
        }
    }
}
