package org.apereo.cas.services;

import org.apereo.cas.CoreAttributesTestUtils;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ReturnMappedAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnMappedAttributeReleasePolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyAttributeMappingWorksForCollections() throws IOException {
        val map = new TreeMap();
        map.put("test1", "newTest1");
        map.put("test2", Stream.of("newTest2", "DaTest2").collect(Collectors.toList()));
        val policyWritten = new ReturnMappedAttributeReleasePolicy(map);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnMappedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);

        val mapValues = new HashMap<String, Object>();
        mapValues.put("test1", "AttributeValue1");
        mapValues.put("test2", "AttributeValue2");

        val principal = CoreAttributesTestUtils.getPrincipal("user", mapValues);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyRead);

        val attributes = policyRead.getAttributes(principal, CoreAttributesTestUtils.getService(), registeredService);
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
        allowedAttributes.put("attr1", "groovy { logger.debug('Running script...'); return 'DOMAIN\\\\' + attributes['uid'] }");
        val wrap = CollectionUtils.<String, Object>wrap(allowedAttributes);
        val policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        val principalAttributes = new HashMap<String, Object>();
        principalAttributes.put("uid", CoreAttributesTestUtils.CONST_USERNAME);
        val result = policyWritten.getAttributes(
            CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes),
            CoreAttributesTestUtils.getService(), registeredService);
        assertTrue(result.containsKey("attr1"));
        assertTrue(result.containsValue("DOMAIN\\" + CoreAttributesTestUtils.CONST_USERNAME));
    }

    @Test
    public void verifyInlinedGroovyMultipleAttributes() {
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        allowedAttributes.put("attr1", "groovy { logger.debug('Running script...'); return ['one', 'two'] }");
        val wrap = CollectionUtils.<String, Object>wrap(allowedAttributes);
        val policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        val principalAttributes = new HashMap<String, Object>();
        principalAttributes.put("uid", CoreAttributesTestUtils.CONST_USERNAME);
        val result = policyWritten.getAttributes(
            CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes),
            CoreAttributesTestUtils.getService(), registeredService);
        assertTrue(result.containsKey("attr1"));
        assertEquals(2, Collection.class.cast(result.get("attr1")).size());
    }

    @Test
    public void verifyExternalGroovyAttributes() throws Exception {
        val file = new File(FileUtils.getTempDirectoryPath(), "script.groovy");
        FileUtils.write(file, "logger.debug('Running script...'); return 'DOMAIN\\\\' + attributes['uid']", StandardCharsets.UTF_8);
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        allowedAttributes.put("attr1", "file:" + file.getCanonicalPath());
        val wrap = CollectionUtils.<String, Object>wrap(allowedAttributes);
        val policyWritten = new ReturnMappedAttributeReleasePolicy(wrap);
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        val principalAttributes = new HashMap<String, Object>();
        principalAttributes.put("uid", CoreAttributesTestUtils.CONST_USERNAME);
        val result = policyWritten.getAttributes(
            CoreAttributesTestUtils.getPrincipal(CoreAttributesTestUtils.CONST_USERNAME, principalAttributes),
            CoreAttributesTestUtils.getService(), registeredService);
        assertTrue(result.containsKey("attr1"));
        assertTrue(result.containsValue("DOMAIN\\" + CoreAttributesTestUtils.CONST_USERNAME));
    }


    @Test
    public void verifyMappingWithoutAttributeValue() {
        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        val mappedAttribute = "urn:oid:0.9.2342.19200300.100.1.3";
        allowedAttributes.put("email", mappedAttribute);
        val policy = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowedAttributes));
        val registeredService = CoreAttributesTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);
        val principalAttributes = new HashMap<String, Object>();
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
