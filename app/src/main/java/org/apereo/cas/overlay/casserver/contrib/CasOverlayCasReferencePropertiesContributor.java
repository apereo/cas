package org.apereo.cas.overlay.casserver.contrib;

import org.apereo.cas.initializr.contrib.TemplatedProjectContributor;
import org.apereo.cas.metadata.CasConfigurationMetadataCatalog;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;

import io.spring.initializr.generator.project.ProjectDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class CasOverlayCasReferencePropertiesContributor extends TemplatedProjectContributor {
    public CasOverlayCasReferencePropertiesContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./etc/cas/config/all-cas-properties.ref", "classpath:/overlay/etc/cas/config/all-cas-properties.ref");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        return CasConfigurationMetadataCatalog.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
                .build());
    }
}
