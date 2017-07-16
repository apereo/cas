package org.apereo.cas.metadata.server;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is {@link CasConfigurationMetadataServerController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RestController("casConfigurationMetadataServerController")
@RequestMapping("/config/metadata")
public class CasConfigurationMetadataServerController {

    /**
     * Find cas configuration property metadata response entity.
     *
     * @param propertyName the property name
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping("/property")
    public ResponseEntity<ConfigurationMetadataProperty> findByPropertyName(@RequestParam("name") final String propertyName) throws Exception {
        final CasConfigurationMetadataRepository repository = new CasConfigurationMetadataRepository();
        final ConfigurationMetadataProperty configMetadataProp = repository.getRepository().getAllProperties().get(propertyName);
        return ResponseEntity.ok(configMetadataProp);
    }
}
