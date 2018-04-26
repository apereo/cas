package org.apereo.cas.metadata.rest;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.junit.Test;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link ConfigurationMetadataSearchResultTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ConfigurationMetadataSearchResultTests {
    @Test
    public void verifyAction() {
        final CasConfigurationMetadataRepository repository = new CasConfigurationMetadataRepository();
        final Map<String, ConfigurationMetadataProperty> properties = repository.getRepository().getAllProperties();
        final ConfigurationMetadataProperty prop = properties.get("server.port");
        assertNotNull(prop);
        final ConfigurationMetadataSearchResult r = new ConfigurationMetadataSearchResult(prop, repository);
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
