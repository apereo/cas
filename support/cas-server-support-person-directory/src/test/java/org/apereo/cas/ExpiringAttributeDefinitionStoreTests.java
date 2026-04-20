package org.apereo.cas;

import module java.base;
import org.apereo.cas.authentication.attribute.AbstractAttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ExpiringAttributeDefinitionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@SpringBootTest(classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class)
@Tag("Attributes")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ExpiringAttributeDefinitionStoreTests {
    private static final String OLD_NAME = "urn:oid:1.3.6.1.4.1.5923.1.1.1.6";
    private static final String NEW_NAME = "urn:oid:1.3.6.1.4.1.5923.2.3.3.6";

    @Test
    void verifyOperation() {
        try (val store = new ExpiringAttributeDefinitionStore()) {
            store.setScope("example.org");
            val defn = DefaultAttributeDefinition
                .builder()
                .key("eduPersonPrincipalName")
                .name(OLD_NAME)
                .expiration("PT2S")
                .build();

            store.registerAttributeDefinition(defn);
            assertFalse(store.getAttributeDefinitionsMap().isEmpty());
            store.registerAttributeDefinitionInSource(defn.withName(NEW_NAME));

            try {
                Thread.sleep(3000);
                assertTrue(store.getAttributeDefinitionsMap().isEmpty());
                assertTrue(store.locateAttributeDefinitionByName(OLD_NAME).isEmpty());
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
            val definition = store.locateAttributeDefinition("eduPersonPrincipalName").orElseThrow();
            assertEquals(NEW_NAME, definition.getName());
            assertFalse(store.getAttributeDefinitions().isEmpty());
        }
    }

    private static final class ExpiringAttributeDefinitionStore extends AbstractAttributeDefinitionStore {
        private final Map<String, AttributeDefinition> store = new ConcurrentHashMap<>();

        @Override
        public Optional<AttributeDefinition> locateAttributeDefinition(final String key) {
            val result = super.locateAttributeDefinition(key);
            if (result.isEmpty()) {
                val newDefn = store.get(key);
                if (newDefn != null) {
                    registerAttributeDefinition(newDefn);
                    return Optional.of(newDefn);
                }
            }
            return Optional.empty();
        }

        private void registerAttributeDefinitionInSource(final AttributeDefinition definition) {
            store.put(definition.getKey(), definition);
        }
    }
}
