package org.apereo.cas.authentication.attribute;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

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

    protected static AttributeDefinition getAttributeDefinition() {
        return DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("uid")
            .friendlyName("eduPersonPrincipalName")
            .scoped(true)
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .script("groovy { return 'hello' }")
            .build();
    }

    @Test
    public void verifyOperation() {
        val store = new DefaultAttributeDefinitionStore();
        store.registerAttributeDefinition(getAttributeDefinition());
        assertNotNull(store.locateAttributeDefinition("eduPersonPrincipalName"));
        assertFalse(store.getAttributeDefinitions().isEmpty());
    }

    @Test
    public void verifySerialization() throws Exception {
        val defn = getAttributeDefinition();
        MAPPER.writeValue(JSON_FILE, defn);
        val read = MAPPER.readValue(JSON_FILE, AttributeDefinition.class);
        assertEquals(read, defn);
    }

    @Test
    public void verifyStoreSerialization() throws Exception {
        val store = new DefaultAttributeDefinitionStore(getAttributeDefinition());
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
