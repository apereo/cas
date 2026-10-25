package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.CoreAttributesTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("AttributeRelease")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class
})
class ReturnLinkedAttributeReleasePolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifySerializeToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policy = new ReturnLinkedAttributeReleasePolicy();
        policy.setAllowedAttributes(CollectionUtils.wrap("uid", List.of("cn", "givenName", "unknown", "firstName")));
        MAPPER.writeValue(jsonFile, policy);
        val policyRead = MAPPER.readValue(jsonFile, ReturnLinkedAttributeReleasePolicy.class);
        assertEquals(policy, policyRead);
    }

    @Test
    void verifyFirstLinkedAttributeWithValueIsReleased() throws Throwable {
        val allowed = CollectionUtils.<String, Object>wrap("primaryGroup", List.of("missing", "groupMembership2", "groupMembership1"));
        val policy = new ReturnLinkedAttributeReleasePolicy().setAllowedAttributes(allowed);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val attributes = new HashMap<String, List<Object>>();
        attributes.put("groupMembership1", List.of("STUD"));
        attributes.put("groupMembership2", List.of("ADMN"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, attributes))
            .build();

        val result = policy.getAttributes(releasePolicyContext);
        assertEquals(List.of("ADMN"), result.get("primaryGroup"));
        assertFalse(result.containsKey("groupMembership1"));
    }

    @Test
    void verifyMappedToMultipleAttributes() throws Throwable {
        val allowed1 = CollectionUtils.<String, Object>wrap("uid", List.of("cn", "givenName", "unknown", "firstName"));
        val p1 = new ReturnLinkedAttributeReleasePolicy().setAllowedAttributes(allowed1);
        val service1 = CoreAttributesTestUtils.getRegisteredService();
        when(service1.getAttributeReleasePolicy()).thenReturn(p1);

        val attributes = new HashMap<String, List<Object>>();
        attributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        attributes.put("firstName", List.of("bob", "robert"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service1)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, attributes))
            .build();

        val result = p1.getAttributes(releasePolicyContext);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("uid"));
        assertTrue(result.get("uid").contains("bob"));
        assertTrue(result.get("uid").contains("robert"));
    }
}
