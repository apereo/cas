package org.apereo.cas.multitenancy;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTenantsManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Web")
@ExtendWith(CasTestExtension.class)
class DefaultTenantsManagerTests {

    static {
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName(), "P@$$w0rd");
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.INITIALIZATION_VECTOR.getPropertyName(), "true");
    }

    @Nested
    @SpringBootTest(classes = BaseMultitenancyTests.SharedTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
    class DefaultTests {
        @Autowired
        @Qualifier(TenantsManager.BEAN_NAME)
        private TenantsManager tenantsManager;

        @Test
        void verifyOperation() {
            val definition = tenantsManager.findTenant("b9584c42");
            assertTrue(definition.isPresent());
            val hostedDefinition = tenantsManager.findTenant("hosted").orElseThrow();
            assertEquals("sso.system.org", hostedDefinition.getProperties().get("cas.host.name"));
            assertSame(hostedDefinition, tenantsManager.findTenant("HOSTED").orElseThrow());
            assertTrue(tenantsManager.findTenant(null).isEmpty());
            assertTrue(tenantsManager.findTenant(StringUtils.EMPTY).isEmpty());
            assertThrows(IllegalArgumentException.class,
                () -> tenantsManager.save(TenantDefinition.builder().id("../shire").build()));
            assertTrue(tenantsManager.findTenant("../shire").isEmpty());
        }

        @Test
        void verifyUnreadableResourceIsNotOverwritten() throws Exception {
            val file = Files.createTempFile("tenants", ".json");
            val manager = new DefaultTenantsManager(JacksonObjectMapperFactory.builder()
                .defaultTypingEnabled(true).build().toObjectMapper(), new FileSystemResource(file));
            val existing = TenantDefinition.builder().id(UUID.randomUUID().toString()).build();
            manager.save(existing);
            assertTrue(manager.findTenant(existing.getId()).isPresent());

            Files.writeString(file, "[ \"java.util.ArrayList\", [ { \"id\": ");
            val definition = TenantDefinition.builder().id(UUID.randomUUID().toString()).build();
            assertThrows(RuntimeException.class, () -> manager.save(definition));
            assertThrows(RuntimeException.class, () -> manager.delete(existing.getId()));
            assertTrue(manager.findTenant(existing.getId()).isPresent());
            assertTrue(manager.findTenant(definition.getId()).isEmpty());
            assertEquals("[ \"java.util.ArrayList\", [ { \"id\": ", Files.readString(file));
        }
    }

    @Nested
    @SpringBootTest(classes = BaseMultitenancyTests.SharedTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=file://${java.io.tmpdir}/Tenants-${random.uuid}.json"
    })
    class ExternalTenantsTests {
        @Autowired
        @Qualifier(TenantsManager.BEAN_NAME)
        private TenantsManager tenantsManager;

        @Autowired
        private CasConfigurationProperties casProperties;

        @Test
        void verifyOperation() {
            val id = UUID.randomUUID().toString();
            var definition = tenantsManager.findTenant(id);
            assertFalse(definition.isPresent());
            tenantsManager.save(
                TenantDefinition.builder()
                    .id(id)
                    .properties(CollectionUtils.wrap("cas.host.name", "sso.system.org"))
                    .build());
            tenantsManager.load();
            definition = tenantsManager.findTenant(id);
            assertTrue(definition.isPresent());
            assertTrue(tenantsManager.delete(id));
        }

        @Test
        void verifyStoredValuesAreWrittenBack() throws Exception {
            val id = UUID.randomUUID().toString();
            val encryptedHost = "{cas-cipher}xoLrkVhqnyAmMHqxWi3t+AcXf/w6Mg3bltpdP1kmG9E=";
            tenantsManager.save(TenantDefinition.builder()
                .id(id)
                .properties(CollectionUtils.wrap("cas.host.name", encryptedHost, "cas.authn.accept.users", "casuser::Mellon"))
                .build());
            assertEquals("sso.system.org", tenantsManager.findTenant(id).orElseThrow().getProperties().get("cas.host.name"));
            val file = casProperties.getMultitenancy().getJson().getLocation().getFile().toPath();
            assertTrue(Files.readString(file).contains(encryptedHost));

            tenantsManager.save(TenantDefinition.builder()
                .id(id)
                .properties(CollectionUtils.wrap("cas.host.name", TenantsManager.REDACTED_PROPERTY_VALUE,
                    "cas.authn.accept.users", "casuser::Changed",
                    "cas.authn.accept.name", TenantsManager.REDACTED_PROPERTY_VALUE))
                .build());
            val properties = tenantsManager.findTenant(id).orElseThrow().getProperties();
            assertEquals("sso.system.org", properties.get("cas.host.name"));
            assertEquals("casuser::Changed", properties.get("cas.authn.accept.users"));
            assertFalse(properties.containsKey("cas.authn.accept.name"));
            val contents = Files.readString(file);
            assertTrue(contents.contains(encryptedHost));
            assertFalse(contents.contains(TenantsManager.REDACTED_PROPERTY_VALUE));
            assertTrue(tenantsManager.delete(id));
            assertTrue(Files.readString(file).contains("java.util.ArrayList"));
            assertTrue(tenantsManager.findTenants().stream().noneMatch(definition -> definition.getId().equals(id)));
        }
    }
}
