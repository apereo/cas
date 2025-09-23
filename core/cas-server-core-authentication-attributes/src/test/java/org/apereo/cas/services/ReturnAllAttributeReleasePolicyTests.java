package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("AttributeRelease")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ReturnAllAttributeReleasePolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifySerializeAReturnAllAttributeReleasePolicyToJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policyWritten = new ReturnAllAttributeReleasePolicy();
        policyWritten.setExcludedAttributes(CollectionUtils.wrapSet("Hello", "World"));
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, ReturnAllAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    void verifyExclusionRules() throws Throwable {
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
            .applicationContext(applicationContext)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertEquals(1, results.size());
        assertFalse(results.containsKey("cn"));
        assertTrue(results.containsKey("uid"));
    }

    @Test
    void verifyConsentForServiceInDisabled() throws Throwable {
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
            .applicationContext(applicationContext)
            .build();
        val consented = policy.getConsentableAttributes(releasePolicyContext);
        assertTrue(consented.isEmpty());
    }

    @Test
    void verifyConsentForServiceInUndefined() throws Throwable {
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
            .applicationContext(applicationContext)
            .build();
        val consented = policy.getConsentableAttributes(releasePolicyContext);
        assertEquals(1, consented.size());
        assertTrue(consented.containsKey("cn"));
    }


    @Test
    void verifyExcludedServicesFromConsent() throws Throwable {
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
            .applicationContext(applicationContext)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertTrue(results.containsKey("cn"));
        assertTrue(results.containsKey("uid"));

        val consented = policy.getConsentableAttributes(releasePolicyContext);
        assertTrue(consented.isEmpty());
    }

    @Test
    void verifyNoConsentPolicy() throws Throwable {
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
            .applicationContext(applicationContext)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertTrue(results.containsKey("cn"));
        assertTrue(results.containsKey("uid"));

        val consented = policy.getConsentableAttributes(releasePolicyContext);
        assertEquals(results, consented);
    }

    @Test
    void verifyConsentPolicyActive() throws Throwable {
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
            .applicationContext(applicationContext)
            .build();

        val consented = policy.getConsentableAttributes(releasePolicyContext);
        assertEquals(1, consented.size());
        assertTrue(consented.containsKey("cn"));
    }


}
