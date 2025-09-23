package org.apereo.cas.metadata;

import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationMetadataRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("CasConfiguration")
class CasConfigurationMetadataRepositoryTests {

    @Test
    void verifyOperation() {
        val repository = new CasConfigurationMetadataRepository();
        var properties = repository.getPropertiesWithType(LdapAuthenticationProperties.class);
        assertFalse(properties.isEmpty());
        properties = repository.getPropertiesWithType(Set.class);
        assertFalse(properties.isEmpty());
    }

    @Test
    void verifyQueryOperation() throws Throwable {
        var properties = CasConfigurationMetadataCatalog.query(ConfigurationMetadataCatalogQuery
            .builder()
            .build());
        assertFalse(properties.properties().isEmpty());

        val file = Files.createTempFile("config", ".yml").toFile();
        CasConfigurationMetadataCatalog.export(file, properties);
        assertTrue(file.exists());

        properties = CasConfigurationMetadataCatalog.query(ConfigurationMetadataCatalogQuery
            .builder()
            .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
            .build());
        assertFalse(properties.properties().isEmpty());

        properties = CasConfigurationMetadataCatalog.query(ConfigurationMetadataCatalogQuery
            .builder()
            .modules(List.of("some-module-name"))
            .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.THIRD_PARTY)
            .build());
        assertTrue(properties.properties().isEmpty());
    }
}
