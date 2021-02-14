package org.apereo.cas.overlay.casserver.contrib;

import org.apereo.cas.initializr.contrib.TemplatedProjectContributor;
import org.apereo.cas.metadata.CasConfigurationMetadataCatalog;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;

import io.spring.initializr.generator.project.ProjectDescription;
import org.springframework.context.ApplicationContext;

public class CasOverlayConfigurationPropertiesContributor extends TemplatedProjectContributor {

    public CasOverlayConfigurationPropertiesContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./etc/cas/config/cas.properties", "classpath:/overlay/etc/cas/config/cas.properties");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        return CasConfigurationMetadataCatalog.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
                .build());
    }

}
