package org.apereo.cas.services;

import org.apereo.cas.CoreAttributesTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.scripting.GroovyScriptResourceCacheManager;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
public class ReturnMappedAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnMappedAttributeReleasePolicy.json");

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
    public void verifyAttributeMappingWorksForCollections() throws IOException {
        val map = new TreeMap();
        map.put("test1", "newTest1");
        map.put("test2", Stream.of("newTest2", "DaTest2").collect(Collectors.toList()));
        val policyWritten = new ReturnMappedAttributeReleasePolicy(map);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnMappedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);

        val mapValues = new HashMap<String, List<Object>>();
        mapValues.put("test1", List.of("AttributeValue1"));
        mapValues.put("test2", List.of("AttributeValue2"));

        val principal = CoreAttributesTestUtils.getPrincipal("user", mapValues);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyRead);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAttributesTestUtils.getService())
            .principal(principal)
            .build();
        val attributes = policyRead.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("DaTest2"));
        assertTrue(attributes.containsKey("newTest2"));
        assertTrue(attributes.containsKey("newTest1"));
    }

    @Test
    public void verifySerializeAndReturnMappedAttributeReleasePolicyToJson() throws IOException {
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        allowedAttributes.put("keyOne", "valueOne");
        val wrap = CollectionUtils.<String, Object>wrap(allowedAttributes);
        val policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);

        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnMappedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifyInlinedGroovyAttributes() {
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        allowedAttributes.put("attr1", "groovy { logger.debug('Running script...'); return 'DOMAIN\\\\' + attributes['uid'][0] }");
        val wrap = CollectionUtils.<String, Object>wrap(allowedAttributes);
        val policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        val principalAttributes = new HashMap<String, List<Object>>();
        principalAttributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes))
            .build();
        val result = policyWritten.getAttributes(releasePolicyContext);
        assertTrue(result.containsKey("attr1"));
        assertTrue(result.containsValue(List.of("DOMAIN\\" + CoreAttributesTestUtils.CONST_USERNAME)));
    }

    @Test
    public void verifyInlinedGroovyMultipleAttributes() {
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        allowedAttributes.put("attr1", "groovy { logger.debug('Running script...'); return ['one', 'two'] }");
        val wrap = CollectionUtils.<String, Object>wrap(allowedAttributes);
        val policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        val principalAttributes = new HashMap<String, List<Object>>();
        principalAttributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes))
            .build();
        val result = policyWritten.getAttributes(releasePolicyContext);
        assertTrue(result.containsKey("attr1"));
        assertEquals(2, Collection.class.cast(result.get("attr1")).size());
    }

    @Test
    public void verifyExternalGroovyAttributes() throws Exception {
        val file = new File(FileUtils.getTempDirectoryPath(), "script.groovy");
        val script = IOUtils.toString(
            new ClassPathResource("GroovyMappedAttribute.groovy").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.write(file, script, StandardCharsets.UTF_8);
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        val attributeName = UUID.randomUUID().toString();
        allowedAttributes.put(attributeName, "file:" + file.getCanonicalPath());
        val wrap = CollectionUtils.<String, Object>wrap(allowedAttributes);
        val policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        val principalAttributes = new HashMap<String, List<Object>>();
        principalAttributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes))
            .build();
        val result = policyWritten.getAttributes(releasePolicyContext);
        assertTrue(result.containsKey(attributeName));
        val attr1 = result.get(attributeName);
        assertTrue(attr1.contains("DOMAIN\\" + CoreAttributesTestUtils.CONST_USERNAME));
        assertTrue(attr1.contains("testing"));
    }


    @Test
    public void verifyMappingWithoutAttributeValue() {
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        val mappedAttribute = "urn:oid:0.9.2342.19200300.100.1.3";
        allowedAttributes.put("email", mappedAttribute);
        val policy = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowedAttributes));
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);
        val principalAttributes = new HashMap<String, List<Object>>();
        principalAttributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        var releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes))
            .build();
        var result = policy.getAttributes(releasePolicyContext);
        assertTrue(result.isEmpty());

        principalAttributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        principalAttributes.put("email", List.of("user@example.org"));

        releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes))
            .build();
        result = policy.getAttributes(releasePolicyContext);
        assertTrue(result.containsKey(mappedAttribute));
        assertEquals(List.of("user@example.org"), result.get(mappedAttribute));
    }

    @Test
    public void verifyClasspathGroovy() {
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        val attributeName = UUID.randomUUID().toString();
        allowedAttributes.put(attributeName, "classpath:GroovyMappedAttribute.groovy");
        val wrap = CollectionUtils.<String, Object>wrap(allowedAttributes);
        val policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        val principalAttributes = new HashMap<String, List<Object>>();
        principalAttributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes))
            .build();
        val result = policyWritten.getAttributes(releasePolicyContext);
        assertTrue(result.containsKey(attributeName));
        val attr1 = result.get(attributeName);
        assertTrue(attr1.contains("DOMAIN\\" + CoreAttributesTestUtils.CONST_USERNAME));
        assertTrue(attr1.contains("testing"));
    }


    @Test
    public void verifyInlinedGroovyWithCache() {
        val allowed1 = ArrayListMultimap.<String, Object>create();
        val attributeName = UUID.randomUUID().toString();
        allowed1.put(attributeName, "groovy { return 'v1' }");
        val p1 = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowed1));

        val service1 = CoreAttributesTestUtils.getRegisteredService();
        when(service1.getAttributeReleasePolicy()).thenReturn(p1);

        val attributes = new HashMap<String, List<Object>>();
        attributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service1)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, attributes))
            .build();
        val result1 = p1.getAttributes(releasePolicyContext);
        assertTrue(result1.containsKey(attributeName));
        assertTrue(result1.containsValue(List.of("v1")));

        val manager = ApplicationContextProvider.getScriptResourceCacheManager().get();
        val key = manager.getKeys().iterator().next();
        assertEquals("v1", manager.get(key).execute(ArrayUtils.EMPTY_OBJECT_ARRAY, String.class));

        val allowed2 = ArrayListMultimap.<String, Object>create();
        allowed2.put(attributeName, "groovy { return 'v2' }");
        val p2 = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowed2));

        val service2 = CoreAttributesTestUtils.getRegisteredService();
        when(service2.getAttributeReleasePolicy()).thenReturn(p2);
        val releasePolicyContext2 = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service2)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, attributes))
            .build();
        val result2 = p2.getAttributes(releasePolicyContext2);
        assertTrue(result2.containsKey(attributeName));
        assertTrue(result2.containsValue(List.of("v2")));
    }

    @Test
    public void verifyExternalGroovyWithCache() {
        val allowed1 = ArrayListMultimap.<String, Object>create();
        val attributeName = UUID.randomUUID().toString();

        allowed1.put(attributeName, "classpath:GroovyMappedAttribute.groovy");
        val p1 = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowed1));

        val service1 = CoreAttributesTestUtils.getRegisteredService();
        when(service1.getAttributeReleasePolicy()).thenReturn(p1);

        val attributes = new HashMap<String, List<Object>>();
        attributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service1)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, attributes))
            .build();
        var result = p1.getAttributes(releasePolicyContext);
        assertTrue(result.containsKey(attributeName));
        result = p1.getAttributes(releasePolicyContext);
        assertTrue(result.containsKey(attributeName));
    }

    @Test
    public void verifyMappedExisting() {
        val allowed1 = CollectionUtils.<String, Object>wrap("uid", "my-userid");
        val p1 = new ReturnMappedAttributeReleasePolicy(allowed1);
        val service1 = CoreAttributesTestUtils.getRegisteredService();
        when(service1.getAttributeReleasePolicy()).thenReturn(p1);

        val attributes = new HashMap<String, List<Object>>();
        attributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service1)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, attributes))
            .build();
        var result = p1.getAttributes(releasePolicyContext);
        assertEquals(1, result.size());
        assertFalse(result.containsKey("uid"));
        assertTrue(result.containsKey("my-userid"));

        attributes.clear();
        attributes.put("my-userid", List.of(CoreAttributesTestUtils.CONST_USERNAME));
        result = p1.getAttributes(releasePolicyContext);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("my-userid"));
    }

    @Test
    public void verifyRequestedDefinitions() {
        val allowed1 = CollectionUtils.<String, Object>wrap("uid", "my-userid");
        val policy = new ReturnMappedAttributeReleasePolicy(allowed1);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .build();
        val attributes = policy.determineRequestedAttributeDefinitions(releasePolicyContext);
        assertTrue(attributes.containsAll(policy.getAllowedAttributes().keySet()));
    }

    @Test
    public void verifyInlinedGroovyFailsPartially() {
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        allowedAttributes.put("attr1", "groovy { $bad-script-here$ }");
        allowedAttributes.put("uid", "userId");

        val wrap = CollectionUtils.<String, Object>wrap(allowedAttributes);
        val policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        val principalAttributes = new HashMap<String, List<Object>>();
        principalAttributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes))
            .build();
        val result = policyWritten.getAttributes(releasePolicyContext);
        assertFalse(result.containsKey("attr1"));
        assertTrue(result.containsKey("userId"));
    }

    @Test
    public void verifyExternalGroovyFailsPartially() throws Exception {
        val allowed1 = ArrayListMultimap.<String, Object>create();
        val file = File.createTempFile("something", ".groovy");
        FileUtils.write(file, "bad-data", StandardCharsets.UTF_8);
        allowed1.put("attr1", "file:" + file.getCanonicalPath());
        allowed1.put("uid", "userId");

        val policy = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowed1));
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);
        val principalAttributes = new HashMap<String, List<Object>>();
        principalAttributes.put("uid", List.of(CoreAttributesTestUtils.CONST_USERNAME));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes))
            .build();
        val result = policy.getAttributes(releasePolicyContext);

        assertFalse(result.containsKey("attr1"));
        assertTrue(result.containsKey("userId"));
    }

    @Test
    public void verifyConcurrentScript() throws Exception {
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        allowedAttributes.put("taxId", "groovy { attributes['fiscalNumber'][0] }");
        allowedAttributes.put("uid", "uid");

        val wrap = CollectionUtils.<String, Object>wrap(allowedAttributes);
        val policy = new ReturnMappedAttributeReleasePolicy(wrap);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val service = Executors.newFixedThreadPool(50);
        IntStream.range(0, 1000)
            .forEach(Unchecked.intConsumer(count -> {
                val future = service.submit(() -> {
                    val principalAttributes = new HashMap<String, List<Object>>();
                    val uid = "user" + count;
                    principalAttributes.put("uid", List.of(uid));
                    principalAttributes.put("fiscalNumber", List.of(uid + '-' + RandomUtils.randomAlphabetic(9)));
                    val principal = CoreAttributesTestUtils.getPrincipal(uid, principalAttributes);

                    val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                        .registeredService(registeredService)
                        .service(CoreAuthenticationTestUtils.getService())
                        .principal(principal)
                        .build();
                    var result = policy.getAttributes(releasePolicyContext);
                    assertNotNull(result);
                    assertTrue(result.containsKey("uid"));
                    assertTrue(result.containsKey("taxId"));
                    assertEquals(uid, result.get("uid").get(0));
                    assertTrue(result.get("taxId").get(0).toString().contains(uid));
                });
                future.get();
            }));
        service.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    public void verifyChainDependingOnPreviousAttributes() {
        val policy1 = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap("uid", "my-userid"));
        policy1.setOrder(1);
        val policy2 = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap("new-uid", "groovy {attributes['my-userid'][0]+'-new'}"));
        policy2.setOrder(2);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .build();

        val chain = new ChainingAttributeReleasePolicy();
        chain.addPolicies(policy2, policy1);

        val attributes = chain.getAttributes(releasePolicyContext);
        assertEquals(2, attributes.size());
        assertEquals("test", attributes.get("my-userid").get(0));
        assertEquals("test-new", attributes.get("new-uid").get(0));
    }
}
