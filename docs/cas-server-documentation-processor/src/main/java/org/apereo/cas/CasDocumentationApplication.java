package org.apereo.cas;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link CasDocumentationApplication}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class CasDocumentationApplication {
    public static void main(final String[] args) {
        var results = CasConfigurationMetadataRepository.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
                .build());

        results.properties().forEach(p -> {
            LOGGER.info("[{}]=[{}]", p.getName(), p.getOwner());
        });
    }
}
