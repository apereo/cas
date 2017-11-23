package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class ReturnMappedAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnMappedAttributeReleasePolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyAttributeMappingWorksForCollections() throws IOException {
        final TreeMap map = new TreeMap();
        map.put("test1", "newTest1");
        map.put("test2", Stream.of("newTest2", "DaTest2").collect(Collectors.toList()));
        final ReturnMappedAttributeReleasePolicy policyWritten = new ReturnMappedAttributeReleasePolicy(map);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        final ReturnMappedAttributeReleasePolicy policyRead = MAPPER.readValue(JSON_FILE, ReturnMappedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);

        final Map<String, Object> mapValues = new HashMap<>();
        mapValues.put("test1", "AttributeValue1");
        mapValues.put("test2", "AttributeValue2");

        final Principal principal = CoreAuthenticationTestUtils.getPrincipal("user", mapValues);
        final RegisteredService registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyRead);

        final Map attributes = policyRead.getAttributes(principal, CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(attributes.containsKey("DaTest2"));
        assertTrue(attributes.containsKey("newTest2"));
        assertTrue(attributes.containsKey("newTest1"));
    }

    @Test
    public void verifySerializeAndReturnMappedAttributeReleasePolicyToJson() throws IOException {
        final Multimap<String, String> allowedAttributes = ArrayListMultimap.create();
        allowedAttributes.put("keyOne", "valueOne");
        final ReturnMappedAttributeReleasePolicy policyWritten =
                new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowedAttributes));

        MAPPER.writeValue(JSON_FILE, policyWritten);
        final RegisteredServiceAttributeReleasePolicy policyRead = MAPPER.readValue(JSON_FILE, ReturnMappedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifyInlinedGroovyAttributes() {
        final Multimap<String, String> allowedAttributes = ArrayListMultimap.create();
        allowedAttributes.put("attr1", "groovy { logger.debug('Running script...'); return 'DOMAIN\\\\' + attributes['uid'] }");
        final ReturnMappedAttributeReleasePolicy policyWritten =
                new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowedAttributes));
        final RegisteredService registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        final Map<String, Object> principalAttributes = new HashMap<>();
        principalAttributes.put("uid", CoreAuthenticationTestUtils.CONST_USERNAME);
        final Map<String, Object> result = policyWritten.getAttributes(
                CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, principalAttributes),
                CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(result.containsKey("attr1"));
        assertTrue(result.containsValue("DOMAIN\\" + CoreAuthenticationTestUtils.CONST_USERNAME));
    }
    
    @Test
    public void verifyMappingWithoutAttributeValue() {
        final Multimap<String, String> allowedAttributes = ArrayListMultimap.create();
        final String mappedAttribute = "urn:oid:0.9.2342.19200300.100.1.3";
        allowedAttributes.put("email", mappedAttribute);
        final ReturnMappedAttributeReleasePolicy policy = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowedAttributes));
        final RegisteredService registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);
        final Map<String, Object> principalAttributes = new HashMap<>();
        principalAttributes.put("uid", CoreAuthenticationTestUtils.CONST_USERNAME);
        Map<String, Object> result = policy.getAttributes(
                CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, principalAttributes),
                CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(result.isEmpty());

        principalAttributes.put("uid", CoreAuthenticationTestUtils.CONST_USERNAME);
        principalAttributes.put("email", "user@example.org");
        result = policy.getAttributes(
                CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, principalAttributes),
                CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(result.containsKey(mappedAttribute));
        assertEquals(result.get(mappedAttribute), "user@example.org");
    }
}
