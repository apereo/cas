package org.apereo.cas.services;

import org.apereo.cas.CoreAttributesTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.GroovyScriptResourceCacheManager;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("Attributes")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
public class ReturnLinkedAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "ReturnLinkedAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @BeforeEach
    public void beforeEach() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            new GroovyScriptResourceCacheManager(), ScriptResourceCacheManager.BEAN_NAME);
        ApplicationContextProvider.getScriptResourceCacheManager()
            .ifPresent(ScriptResourceCacheManager::clear);
    }

    @Test
    public void verifySerializeToJson() throws IOException {
        val policy = new ReturnLinkedAttributeReleasePolicy();
        policy.setAllowedAttributes(CollectionUtils.wrap("uid", List.of("cn", "givenName", "unknown", "firstName")));
        MAPPER.writeValue(JSON_FILE, policy);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnLinkedAttributeReleasePolicy.class);
        assertEquals(policy, policyRead);
    }

    @Test
    public void verifyMappedToMultipleAttributes() {
        val allowed1 = CollectionUtils.<String, Object>wrap("uid", List.of("cn", "givenName", "unknown", "firstName"));
        val p1 = new ReturnLinkedAttributeReleasePolicy().setAllowedAttributes(allowed1);
        val service1 = CoreAttributesTestUtils.getRegisteredService();
        when(service1.getAttributeReleasePolicy()).thenReturn(p1);

        val attributes = new HashMap<String, List<Object>>();
        attributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        attributes.put("firstName", List.of("bob", "robert"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service1)
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
