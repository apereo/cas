package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("Attributes")
public class ReturnAllAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnAllAttributeReleasePolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @BeforeEach
    public void setup() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, CasConfigurationProperties.class,
            CasConfigurationProperties.class.getSimpleName());
        ApplicationContextProvider.holdApplicationContext(applicationContext);
    }

    @Test
    public void verifySerializeAReturnAllAttributeReleasePolicyToJson() throws IOException {
        val policyWritten = new ReturnAllAttributeReleasePolicy();
        policyWritten.setExcludedAttributes(CollectionUtils.wrapSet("Hello", "World"));
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnAllAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifyExclusionRules() {
        val policy = new ReturnAllAttributeReleasePolicy();
        policy.setExcludedAttributes(CollectionUtils.wrapSet("cn"));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", List.of("CommonName"), "uid", List.of("casuser")));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertEquals(1, results.size());
        assertFalse(results.containsKey("cn"));
        assertTrue(results.containsKey("uid"));
    }

    @Test
    public void verifyConsentForServiceInDisabled() {
        val policy = new ReturnAllAttributeReleasePolicy();
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("cn", List.of("CommonName")));
        val consentPolicy = new DefaultRegisteredServiceConsentPolicy();
        consentPolicy.setIncludeOnlyAttributes(Set.of("cn"));
        consentPolicy.setStatus(TriStateBoolean.FALSE);
        policy.setConsentPolicy(consentPolicy);

        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val consented = policy.getConsentableAttributes(releasePolicyContext);
        assertTrue(consented.isEmpty());
    }

    @Test
    public void verifyConsentForServiceInUndefined() {
        val policy = new ReturnAllAttributeReleasePolicy();
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("cn", List.of("CommonName")));
        val consentPolicy = new DefaultRegisteredServiceConsentPolicy();
        consentPolicy.setIncludeOnlyAttributes(Set.of("cn"));
        consentPolicy.setStatus(TriStateBoolean.UNDEFINED);
        policy.setConsentPolicy(consentPolicy);
        policy.postLoad();

        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val consented = policy.getConsentableAttributes(releasePolicyContext);
        assertEquals(1, consented.size());
        assertTrue(consented.containsKey("cn"));
    }


    @Test
    public void verifyExcludedServicesFromConsent() {
        val policy = new ReturnAllAttributeReleasePolicy();
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", List.of("CommonName"), "uid", List.of("casuser")));

        val consentPolicy = new DefaultRegisteredServiceConsentPolicy();
        consentPolicy.setIncludeOnlyAttributes(Set.of("cn"));
        consentPolicy.setStatus(TriStateBoolean.TRUE);
        consentPolicy.setExcludedServices(Set.of("https://.+"));
        policy.setConsentPolicy(consentPolicy);

        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertTrue(results.containsKey("cn"));
        assertTrue(results.containsKey("uid"));

        val consented = policy.getConsentableAttributes(releasePolicyContext);
        assertTrue(consented.isEmpty());
    }

    @Test
    public void verifyNoConsentPolicy() {
        val policy = new ReturnAllAttributeReleasePolicy();
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", List.of("CommonName"), "uid", List.of("casuser")));
        policy.setConsentPolicy(null);

        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertTrue(results.containsKey("cn"));
        assertTrue(results.containsKey("uid"));

        val consented = policy.getConsentableAttributes(releasePolicyContext);
        assertEquals(results, consented);
    }

    @Test
    public void verifyConsentPolicyActive() {
        val policy = new ReturnAllAttributeReleasePolicy();
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", List.of("CommonName"), "uid", List.of("casuser")));
        val consentPolicy = new DefaultRegisteredServiceConsentPolicy();
        consentPolicy.setIncludeOnlyAttributes(Set.of("cn"));
        consentPolicy.setExcludedAttributes(Set.of("uid"));
        consentPolicy.setStatus(TriStateBoolean.TRUE);
        policy.setConsentPolicy(consentPolicy);

        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();

        val consented = policy.getConsentableAttributes(releasePolicyContext);
        assertEquals(1, consented.size());
        assertTrue(consented.containsKey("cn"));
    }


}
