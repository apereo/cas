package org.apereo.cas.metadata;

import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
public class CasConfigurationMetadataRepositoryTests {

    @Test
    public void verifyOperation() {
        val repository = new CasConfigurationMetadataRepository();
        var properties = repository.getPropertiesWithType(LdapAuthenticationProperties.class);
        assertTrue(properties.isEmpty());
        properties = repository.getPropertiesWithType(GoogleAuthenticatorMultifactorProperties.class);
        assertTrue(properties.isEmpty());
        properties = repository.getPropertiesWithType(Set.class);
        assertFalse(properties.isEmpty());
    }

    @Test
    public void verifyQueryOperation() {
        val repository = new CasConfigurationMetadataRepository();
        var properties = repository.query(ConfigurationMetadataCatalogQuery.builder().build());
        assertFalse(properties.properties().isEmpty());
        properties = repository.query(ConfigurationMetadataCatalogQuery.builder().casExclusive(true).build());
        assertTrue(properties.properties().isEmpty());
        properties = repository.query(ConfigurationMetadataCatalogQuery.builder()
            .modules(List.of("some-module-name"))
            .casExclusive(true).build());
        assertTrue(properties.properties().isEmpty());
    }

}
