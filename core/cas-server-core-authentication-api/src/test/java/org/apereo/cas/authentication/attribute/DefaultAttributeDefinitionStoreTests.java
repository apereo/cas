package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAttributeDefinitionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class DefaultAttributeDefinitionStoreTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "DefaultAttributeDefinitionStoreTests.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyAttrDefnNotFound() {
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("invalid")
            .scoped(true)
            .build();
        store.registerAttributeDefinition(defn);
        var values = store.resolveAttributeValues("whatever", CoreAuthenticationTestUtils.getAttributes());
        assertTrue(values.isEmpty());
        values = store.resolveAttributeValues("eduPersonPrincipalName", CoreAuthenticationTestUtils.getAttributes());
        assertTrue(values.isEmpty());
    }

    @Test
    public void verifyScopedAttrDefn() {
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("uid")
            .scoped(true)
            .build();
        store.registerAttributeDefinition(defn);
        var values = (Optional<List<Object>>) store.resolveAttributeValues("eduPersonPrincipalName", CoreAuthenticationTestUtils.getAttributes());
        assertTrue(values.isPresent());
        assertTrue(values.get().contains("test@example.org"));
    }

    @Test
    public void verifyScriptedEmbeddedAttrDefn() {
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("uid")
            .scoped(true)
            .script("groovy { logger.info(\" name: ${attributeName}, values: ${attributeValues} \"); return ['hello', 'world'] } ")
            .build();
        store.registerAttributeDefinition(defn);
        var values = (Optional<List<Object>>) store.resolveAttributeValues("eduPersonPrincipalName", CoreAuthenticationTestUtils.getAttributes());
        assertTrue(values.isPresent());
        assertTrue(values.get().contains("hello@example.org"));
        assertTrue(values.get().contains("world@example.org"));
    }

    @Test
    public void verifyScriptedExternalAttrDefn() {
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("system.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("uid")
            .scoped(true)
            .script("classpath:/attribute-definition.groovy")
            .build();
        store.registerAttributeDefinition(defn);
        var values = (Optional<List<Object>>) store.resolveAttributeValues("eduPersonPrincipalName", CoreAuthenticationTestUtils.getAttributes());
        assertTrue(values.isPresent());
        assertTrue(values.get().contains("casuser@system.org"));
        assertTrue(values.get().contains("groovy@system.org"));
    }

    @Test
    public void verifyFormattedAttrDefn() {
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("givenName")
            .scoped(true)
            .patternFormat("hello,{0}")
            .build();
        store.registerAttributeDefinition(defn);
        var values = (Optional<List<Object>>) store.resolveAttributeValues("eduPersonPrincipalName", CoreAuthenticationTestUtils.getAttributes());
        assertTrue(values.isPresent());
        assertTrue(values.get().contains("hello,test@example.org"));
    }

    @Test
    public void verifyOperation() {
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .friendlyName("eduPersonPrincipalName")
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();

        store.registerAttributeDefinition(defn);
        assertNotNull(store.locateAttributeDefinition("eduPersonPrincipalName"));
        assertFalse(store.getAttributeDefinitions().isEmpty());
    }

    @Test
    public void verifySerialization() throws Exception {
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .friendlyName("eduPersonPrincipalName")
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();
        MAPPER.writeValue(JSON_FILE, defn);
        val read = MAPPER.readValue(JSON_FILE, AttributeDefinition.class);
        assertEquals(read, defn);
    }

    @Test
    public void verifyStoreSerialization() throws Exception {
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .friendlyName("eduPersonPrincipalName")
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();
        val store = new DefaultAttributeDefinitionStore(defn);
        store.setScope("example.org");
        val file = File.createTempFile("attr", "json");
        store.to(file);
        assertTrue(file.exists());
        val store2 = DefaultAttributeDefinitionStore.from(new FileSystemResource(file));
        assertEquals(store2, store);
    }

    @Test
    public void verifyExternalImport() {
        val store = DefaultAttributeDefinitionStore.from(new ClassPathResource("AttributeDefns.json"));
        assertFalse(store.getAttributeDefinitions().isEmpty());
        assertNotNull(store.locateAttributeDefinition("eduPersonPrincipalName"));
    }
}
