package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.attribute.CaseCanonicalizationMode;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
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
@Tag("AttributeRelease")
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(CasTestExtension.class)
class ReturnAllowedAttributeReleasePolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(ReturnAllowedAttributeReleasePolicyTestConfiguration.class)
    @EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class})
    static class BaseAttributeTests {
    }

    @SpringBootTest(
        classes = BaseAttributeTests.class,
        properties = {
            "cas.authn.attribute-repository.stub.attributes.uid=uid",
            "cas.authn.attribute-repository.stub.attributes.mail=cas@apereo.org",
            "cas.authn.attribute-repository.stub.attributes.eduPersonAffiliation=developer",
            "cas.authn.attribute-repository.stub.attributes.groupMembership=adopters",
            "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/return-allowed-definitions.json"
        })
    @EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class})
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
        void verifyVirtualAttributesInChain() {
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

    @SpringBootTest(
        classes = BaseAttributeTests.class,
        properties = {
            "cas.authn.attribute-repository.stub.attributes.uid=uid",
            "cas.authn.attribute-repository.stub.attributes.mail=cas@apereo.org",
            "cas.authn.attribute-repository.stub.attributes.eduPersonAffiliation=developer",
            "cas.authn.attribute-repository.stub.attributes.groupMembership=adopters",
            "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/return-allowed-definitions.json",
            "cas.authn.attribute-repository.core.default-attributes-to-release=cn,mail"
        })
    @EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class})
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    class DefaultTests {

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifySerialization() throws IOException {
            val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
            val allowedAttributes = new ArrayList<String>();
            allowedAttributes.add("attributeOne");
            val policyWritten = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
            policyWritten.setCanonicalizationMode(CaseCanonicalizationMode.UPPER.name());
            policyWritten.setPrincipalIdAttribute("principalId");
            MAPPER.writeValue(jsonFile, policyWritten);
            val policyRead = MAPPER.readValue(jsonFile, ReturnAllowedAttributeReleasePolicy.class);
            assertEquals(policyWritten, policyRead);
        }

        @Test
        void verifyInlineGroovy() throws Throwable {
            val allowedAttributes = new ArrayList<String>();
            allowedAttributes.add("uid");
            allowedAttributes.add("groovy { [ fullName: [ 'MyGivenName' + attributes['cn'][0] ] ] } ");
            for (var i = 0; i < 5; i++) {
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
        void verifyRequestedDefinitions() {
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

    @TestConfiguration(value = "ReturnAllowedAttributeReleasePolicyTestConfiguration", proxyBeanMethods = false)
    static class ReturnAllowedAttributeReleasePolicyTestConfiguration {
        @Bean
        public ServicesManager servicesManager() {
            return mock(ChainingServicesManager.class);
        }
    }
}
