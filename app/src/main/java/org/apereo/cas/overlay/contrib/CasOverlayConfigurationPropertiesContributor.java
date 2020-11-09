package org.apereo.cas.overlay.contrib;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;
import org.apereo.cas.overlay.contrib.util.TemplatedProjectContributor;
import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.project.ProjectDescription;
import lombok.val;
import org.springframework.context.ApplicationContext;
import java.util.stream.Collectors;

public class CasOverlayConfigurationPropertiesContributor extends TemplatedProjectContributor {

    public CasOverlayConfigurationPropertiesContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./etc/cas/config/cas.properties", "classpath:/overlay/etc/cas/config/cas.properties");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        val modules = project.getRequestedDependencies().values()
            .stream()
            .map(Dependency::getArtifactId)
            .collect(Collectors.toList());
        val repository = new CasConfigurationMetadataRepository();
        return repository.query(ConfigurationMetadataCatalogQuery.builder()
            .modules(modules)
            .casExclusive(true)
            .build());
    }

}
