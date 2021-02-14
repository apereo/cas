package org.apereo.cas.overlay.casserver.contrib;

import org.apereo.cas.initializr.contrib.TemplatedProjectContributor;
import org.apereo.cas.metadata.CasConfigurationMetadataCatalog;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;

import io.spring.initializr.generator.project.ProjectDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class CasOverlayAllReferencePropertiesContributor extends TemplatedProjectContributor {
    public CasOverlayAllReferencePropertiesContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./etc/cas/config/all-properties.ref", "classpath:/overlay/etc/cas/config/all-properties.ref");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        return CasConfigurationMetadataCatalog.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.ALL)
                .build());
    }
}
