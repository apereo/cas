package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.CoreAttributesTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
@RunWith(JUnit4.class)
@Slf4j
public class ReturnMappedAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnMappedAttributeReleasePolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyAttributeMappingWorksForCollections() throws IOException {
        final var map = new TreeMap();
        map.put("test1", "newTest1");
        map.put("test2", Stream.of("newTest2", "DaTest2").collect(Collectors.toList()));
        final var policyWritten = new ReturnMappedAttributeReleasePolicy(map);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        final var policyRead = MAPPER.readValue(JSON_FILE, ReturnMappedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);

        final Map<String, Object> mapValues = new HashMap<>();
        mapValues.put("test1", "AttributeValue1");
        mapValues.put("test2", "AttributeValue2");

        final var principal = CoreAttributesTestUtils.getPrincipal("user", mapValues);
        final var registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyRead);

        final Map attributes = policyRead.getAttributes(principal, CoreAttributesTestUtils.getService(), registeredService);
        assertTrue(attributes.containsKey("DaTest2"));
        assertTrue(attributes.containsKey("newTest2"));
        assertTrue(attributes.containsKey("newTest1"));
    }

    @Test
    public void verifySerializeAndReturnMappedAttributeReleasePolicyToJson() throws IOException {
        final Multimap<String, Object> allowedAttributes = ArrayListMultimap.create();
        allowedAttributes.put("keyOne", "valueOne");
        final var wrap = CollectionUtils.wrap(allowedAttributes);
        final var policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);

        MAPPER.writeValue(JSON_FILE, policyWritten);
        final RegisteredServiceAttributeReleasePolicy policyRead = MAPPER.readValue(JSON_FILE, ReturnMappedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifyInlinedGroovyAttributes() {
        final Multimap<String, Object> allowedAttributes = ArrayListMultimap.create();
        allowedAttributes.put("attr1", "groovy { logger.debug('Running script...'); return 'DOMAIN\\\\' + attributes['uid'] }");
        final var wrap = CollectionUtils.wrap(allowedAttributes);
        final var policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);
        final var registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        final Map<String, Object> principalAttributes = new HashMap<>();
        principalAttributes.put("uid", CoreAttributesTestUtils.CONST_USERNAME);
        final var result = policyWritten.getAttributes(
            CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes),
            CoreAttributesTestUtils.getService(), registeredService);
        assertTrue(result.containsKey("attr1"));
        assertTrue(result.containsValue("DOMAIN\\" + CoreAttributesTestUtils.CONST_USERNAME));
    }

    @Test
    public void verifyExternalGroovyAttributes() throws Exception {
        final var file = new File(FileUtils.getTempDirectoryPath(), "script.groovy");
        FileUtils.write(file, "logger.debug('Running script...'); return 'DOMAIN\\\\' + attributes['uid']", StandardCharsets.UTF_8);
        final Multimap<String, Object> allowedAttributes = ArrayListMultimap.create();
        allowedAttributes.put("attr1", "file:" + file.getCanonicalPath());
        final var wrap = CollectionUtils.wrap(allowedAttributes);
        final var policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);
        final var registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        final Map<String, Object> principalAttributes = new HashMap<>();
        principalAttributes.put("uid", CoreAttributesTestUtils.CONST_USERNAME);
        final var result = policyWritten.getAttributes(
            CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes),
            CoreAttributesTestUtils.getService(), registeredService);
        assertTrue(result.containsKey("attr1"));
        assertTrue(result.containsValue("DOMAIN\\" + CoreAttributesTestUtils.CONST_USERNAME));
    }


    @Test
    public void verifyMappingWithoutAttributeValue() {
        final Multimap<String, Object> allowedAttributes = ArrayListMultimap.create();
        final var mappedAttribute = "urn:oid:0.9.2342.19200300.100.1.3";
        allowedAttributes.put("email", mappedAttribute);
        final var policy = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowedAttributes));
        final var registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);
        final Map<String, Object> principalAttributes = new HashMap<>();
        principalAttributes.put("uid", CoreAttributesTestUtils.CONST_USERNAME);
        var result = policy.getAttributes(
            CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes),
            CoreAttributesTestUtils.getService(), registeredService);
        assertTrue(result.isEmpty());

        principalAttributes.put("uid", CoreAttributesTestUtils.CONST_USERNAME);
        principalAttributes.put("email", "user@example.org");
        result = policy.getAttributes(
            CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes),
            CoreAttributesTestUtils.getService(), registeredService);
        assertTrue(result.containsKey(mappedAttribute));
        assertEquals("user@example.org", result.get(mappedAttribute));
    }
}
