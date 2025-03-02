package org.apereo.cas.metadata.rest;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConfigurationMetadataSearchResultTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("CasConfiguration")
class ConfigurationMetadataSearchResultTests {
    @Test
    void verifyAction() {
        val repository = new CasConfigurationMetadataRepository();
        val properties = repository.getRepository().getAllProperties();
        val prop = properties.get("server.port");
        assertNotNull(prop);
        val result = new ConfigurationMetadataSearchResult(prop);
        assertEquals(prop.getDefaultValue(), result.getDefaultValue());
        assertEquals(prop.getId(), result.getId());
        assertEquals(prop.getName(), result.getName());
        assertEquals(prop.getType(), result.getType());
        assertEquals(prop.getShortDescription(), result.getShortDescription());
        assertEquals(prop.getDescription(), result.getDescription());
        assertEquals(prop.getDefaultValue(), result.getDefaultValue());
        assertNotNull(result.getGroup());

    }
}
