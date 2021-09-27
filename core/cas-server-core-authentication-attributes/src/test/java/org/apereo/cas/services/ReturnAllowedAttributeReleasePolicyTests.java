package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.scripting.GroovyScriptResourceCacheManager;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("Attributes")
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = "cas.authn.attribute-repository.core.default-attributes-to-release=cn,mail")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReturnAllowedAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnAllowedAttributeReleasePolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    @Order(1)
    public void verifySerializeAReturnAllowedAttributeReleasePolicyToJson() throws IOException {
        val allowedAttributes = new ArrayList<String>();
        allowedAttributes.add("attributeOne");
        val policyWritten = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
        policyWritten.setPrincipalIdAttribute("principalId");
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnAllowedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    @Order(2)
    public void verifyConsentable() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, casProperties,
            CasConfigurationProperties.class.getSimpleName());
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        
        val allowedAttributes = new ArrayList<String>();
        allowedAttributes.add("uid");
        allowedAttributes.add("cn");
        allowedAttributes.add("givenName");
        val policy = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
        val consentPolicy = new DefaultRegisteredServiceConsentPolicy(Set.of("cn"), Set.of("givenName"));
        policy.setConsentPolicy(consentPolicy);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);
        val results = policy.getConsentableAttributes(principal,
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertEquals(1, results.size());
        assertTrue(results.containsKey("givenName"));
    }

    @Test
    @Order(3)
    public void verifyRequestedDefinitions() {
        val allowedAttributes = new ArrayList<String>();
        allowedAttributes.add("uid");
        allowedAttributes.add("cn");
        allowedAttributes.add("givenName");
        val policy = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
        assertTrue(policy.determineRequestedAttributeDefinitions(
            CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService()
        ).containsAll(policy.getAllowedAttributes()));
    }

    @Test
    @Order(4)
    public void verifyRequestedDefinitionsWithExistingPrincipalAttribute() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, casProperties,
            CasConfigurationProperties.class.getSimpleName());
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val allowedAttributes = new ArrayList<String>();
        allowedAttributes.add("custom-name");
        val policy = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);

        val defn = DefaultAttributeDefinition.builder()
            .key("customName")
            .name("custom-name")
            .build();
        val store = new DefaultAttributeDefinitionStore(defn);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, store, AttributeDefinitionStore.BEAN_NAME);

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", Map.of("customName", List.of("CAS")));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val attributes = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(attributes.containsKey("custom-name"));
        assertEquals(attributes.get("custom-Name").get(0), "CAS");
    }

    @Test
    @Order(6)
    public void verifyRequestedDefinitionsWithoutPrincipalAttribute() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, casProperties,
            CasConfigurationProperties.class.getSimpleName());

        val allowedAttributes = new ArrayList<String>();
        allowedAttributes.add("given-name");
        allowedAttributes.add("uid");
        val policy = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);

        val defn = DefaultAttributeDefinition.builder()
            .key("given-name")
            .script("groovy { return ['hello'] }")
            .build();
        val store = new DefaultAttributeDefinitionStore(defn);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, store, AttributeDefinitionStore.BEAN_NAME);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            GroovyScriptResourceCacheManager.class, ScriptResourceCacheManager.BEAN_NAME);
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", Map.of("uid", List.of("UID")));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val attributes = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), registeredService);
        assertEquals(2, attributes.size());
        assertTrue(attributes.containsKey("given-name"));
        assertEquals(attributes.get("given-name").get(0), "hello");
    }

    @Test
    @Order(5)
    public void verifyDefaultAttributes() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, casProperties,
            CasConfigurationProperties.class.getSimpleName());

        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setPrincipalIdAttribute("principalId");
        policy.postLoad();

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getUsernameAttributeProvider()).thenReturn(new RegisteredServiceUsernameAttributeProvider() {
            private static final long serialVersionUID = 6935950848419028873L;

            @Override
            public String resolveUsername(final Principal principal, final Service service, final RegisteredService registeredService) {
                return principal.getId();
            }
        });
        val attributes = policy.getAttributes(principal, CoreAuthenticationTestUtils.getService(), registeredService);
        assertEquals(3, attributes.size());
        assertTrue(attributes.containsKey("principalId"));
        assertTrue(attributes.containsKey("cn"));
        assertTrue(attributes.containsKey("mail"));
    }
}
