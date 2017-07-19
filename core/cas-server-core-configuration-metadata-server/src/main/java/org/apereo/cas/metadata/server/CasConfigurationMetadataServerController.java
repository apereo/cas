package org.apereo.cas.metadata.server;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * This is {@link CasConfigurationMetadataServerController}.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RestController("casConfigurationMetadataServerController")
@RequestMapping(path = "/config/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
public class CasConfigurationMetadataServerController {
    private final CasConfigurationMetadataRepository repository;

    public CasConfigurationMetadataServerController(final CasConfigurationMetadataRepository repository) {
        this.repository = repository;
    }

    /**
     * Find cas configuration property by name.
     *
     * @param propertyName the property name
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(path = "/property")
    public ResponseEntity<ConfigurationMetadataProperty> findByPropertyName(@RequestParam("name") final String propertyName) throws Exception {
        final ConfigurationMetadataProperty configMetadataProp = repository.getRepository().getAllProperties().get(propertyName);
        return ResponseEntity.ok(configMetadataProp);
    }

    /**
     * Find cas configuration group by group name.
     *
     * @param name the property name
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(path = "/group")
    public ResponseEntity<ConfigurationMetadataGroup> findByGroupName(@RequestParam("name") final String name) throws Exception {
        final ConfigurationMetadataGroup grp = repository.getRepository().getAllGroups().get(name);
        return ResponseEntity.ok(grp);
    }

    /**
     * Find all groups.
     *
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(path = "/groups")
    public ResponseEntity<Map<String, ConfigurationMetadataGroup>> findAllGroups() throws Exception {
        return ResponseEntity.ok(repository.getRepository().getAllGroups());
    }

    /**
     * Find all properties.
     *
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(path = "/properties")
    public ResponseEntity<Map<String, ConfigurationMetadataProperty>> findAllProperties() throws Exception {
        return ResponseEntity.ok(repository.getRepository().getAllProperties());
    }
}
