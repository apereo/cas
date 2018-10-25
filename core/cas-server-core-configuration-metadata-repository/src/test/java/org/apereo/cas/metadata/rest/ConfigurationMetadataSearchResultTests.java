package org.apereo.cas.metadata.rest;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConfigurationMetadataSearchResultTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ConfigurationMetadataSearchResultTests {
    @Test
    public void verifyAction() {
        val repository = new CasConfigurationMetadataRepository();
        val properties = repository.getRepository().getAllProperties();
        val prop = properties.get("server.port");
        assertNotNull(prop);
        val r = new ConfigurationMetadataSearchResult(prop, repository);
        assertEquals(prop.getDefaultValue(), r.getDefaultValue());
        assertEquals(prop.getId(), r.getId());
        assertEquals(prop.getName(), r.getName());
        assertEquals(prop.getType(), r.getType());
        assertEquals(prop.getShortDescription(), r.getShortDescription());
        assertEquals(prop.getDescription(), r.getDescription());
        assertEquals(prop.getDefaultValue(), r.getDefaultValue());
        assertNotNull(r.getGroup());

    }
}
