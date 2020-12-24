package org.apereo.cas.overlay.contrib;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;
import org.apereo.cas.initializr.contrib.TemplatedProjectContributor;
import io.spring.initializr.generator.project.ProjectDescription;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;

@Slf4j
public class CasOverlayAllReferencePropertiesContributor extends TemplatedProjectContributor {
    public CasOverlayAllReferencePropertiesContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./etc/cas/config/all-properties.ref", "classpath:/overlay/etc/cas/config/all-properties.ref");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        val repository = new CasConfigurationMetadataRepository();
        return repository.query(ConfigurationMetadataCatalogQuery.builder().casExclusive(false).build());
    }
}
