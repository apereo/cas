package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAttributeDefinitionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=cas-user-id",
        "cas.authn.attribute-repository.stub.attributes.givenName=cas-given-name",
        "cas.authn.attribute-repository.stub.attributes.eppn=casuser",
        "cas.authn.attribute-repository.stub.attributes.mismatchedAttributeKey=someValue",

        "cas.server.scope=cas.org",

        "cas.person-directory.attribute-definition-store.json.location=classpath:/basic-attribute-definitions.json"
    })
@Tag("Simple")
public class DefaultAttributeDefinitionStoreTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "DefaultAttributeDefinitionStoreTests.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Test
    public void verifyReturnAll() {
        val person = attributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);

        val policy = new ReturnAllAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal(person.getAttributes()),
            CoreAuthenticationTestUtils.getService(), CoreAuthenticationTestUtils.getRegisteredService());
        assertNotNull(attributes);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
        assertTrue(List.class.cast(attributes.get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")).contains("cas-user-id@cas.org"));
    }

    @Test
    public void verifyEncryptedAttributeDefinitions() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val servicePublicKey = new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA");
        when(service.getPublicKey()).thenReturn(servicePublicKey);

        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("cn")
            .scoped(true)
            .encrypted(true)
            .build();
        store.registerAttributeDefinition(defn);
        val attrs = store.resolveAttributeValues(CoreAuthenticationTestUtils.getAttributes(), service);
        assertFalse(attrs.isEmpty());
        assertTrue(attrs.containsKey("cn"));
        val values = CollectionUtils.toCollection(attrs.get("cn"));
        assertFalse(values.stream()
            .anyMatch(value -> value.toString().equalsIgnoreCase(CoreAuthenticationTestUtils.CONST_USERNAME)));
    }

    @Test
    public void verifyMismatchedKeyReturnAll() {
        val person = attributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);

        val policy = new ReturnAllAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal(person.getAttributes()),
            CoreAuthenticationTestUtils.getService(), CoreAuthenticationTestUtils.getRegisteredService());
        assertNotNull(attributes);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("interesting-attribute"));
        assertTrue(List.class.cast(attributes.get("interesting-attribute")).contains("cas-given-name@cas.org"));
    }

    @Test
    public void verifyAttrDefnNotFound() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("invalid")
            .scoped(true)
            .build();
        store.registerAttributeDefinition(defn);
        var values = (Optional<Pair<AttributeDefinition, List<Object>>>) store.resolveAttributeValues("whatever",
            CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME), service);
        assertTrue(values.isEmpty());
    }

    @Test
    public void verifyAttributeDefinitionsAsMap() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("cn")
            .scoped(true)
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();
        store.registerAttributeDefinition(defn);
        assertFalse(store.isEmpty());
        val attrs = store.resolveAttributeValues(CoreAuthenticationTestUtils.getAttributes(), service);
        assertFalse(attrs.isEmpty());
        assertTrue(attrs.containsKey("mail"));
        assertTrue(attrs.containsKey(defn.getName()));
        val values = (List<Object>) attrs.get(defn.getName());
        assertTrue(values.contains("TEST@example.org"));
    }

    @Test
    public void verifyScopedAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("uid")
            .scoped(true)
            .build();
        store.registerAttributeDefinition(defn);
        var values = store.resolveAttributeValues("eduPersonPrincipalName",
            CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME), service);
        assertTrue(values.isPresent());
        assertTrue(values.get().getValue().contains("test@example.org"));
    }

    @Test
    public void verifyScriptedEmbeddedAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("uid")
            .scoped(true)
            .script("groovy { logger.info(\" name: ${attributeName}, values: ${attributeValues} \"); return ['hello', 'world'] } ")
            .build();
        store.registerAttributeDefinition(defn);
        var values = store.resolveAttributeValues("eduPersonPrincipalName",
            CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME), service);
        assertTrue(values.isPresent());
        assertTrue(values.get().getValue().contains("hello@example.org"));
        assertTrue(values.get().getValue().contains("world@example.org"));
    }

    @Test
    public void verifyScriptedExternalAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("system.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("uid")
            .scoped(true)
            .script("classpath:/attribute-definition.groovy")
            .build();
        store.registerAttributeDefinition(defn);
        var values = store.resolveAttributeValues("eduPersonPrincipalName",
            CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME), service);
        assertTrue(values.isPresent());
        assertTrue(values.get().getValue().contains("casuser@system.org"));
        assertTrue(values.get().getValue().contains("groovy@system.org"));
    }

    @Test
    public void verifyFormattedAttrDefn() {
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
            .attribute("givenName")
            .scoped(true)
            .patternFormat("hello,{0}")
            .build();
        store.registerAttributeDefinition(defn);
        var values = store.resolveAttributeValues("eduPersonPrincipalName",
            CollectionUtils.wrap(CoreAuthenticationTestUtils.CONST_USERNAME), service);
        assertTrue(values.isPresent());
        assertTrue(values.get().getValue().contains("hello,test@example.org"));
    }

    @Test
    public void verifyOperation() {
        val store = new DefaultAttributeDefinitionStore();
        store.setScope("example.org");
        val defn = DefaultAttributeDefinition.builder()
            .key("eduPersonPrincipalName")
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
            .name("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")
            .build();
        val store = new DefaultAttributeDefinitionStore(defn);
        store.setScope("example.org");
        val file = File.createTempFile("attr", "json");
        store.to(file);
        assertTrue(file.exists());
        val store2 = new DefaultAttributeDefinitionStore(new FileSystemResource(file));
        assertEquals(store2, store);
    }

    @Test
    public void verifyExternalImport() throws Exception {
        val store = new DefaultAttributeDefinitionStore(new ClassPathResource("AttributeDefns.json"));
        assertFalse(store.getAttributeDefinitions().isEmpty());
        assertNotNull(store.locateAttributeDefinition("eduPersonPrincipalName"));
    }
}
