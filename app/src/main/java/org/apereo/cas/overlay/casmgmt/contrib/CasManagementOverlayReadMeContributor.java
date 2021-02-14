package org.apereo.cas.overlay.casmgmt.contrib;

import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import lombok.val;
import org.apereo.cas.initializr.contrib.TemplatedProjectContributor;
import org.springframework.context.ApplicationContext;

public class CasManagementOverlayReadMeContributor extends TemplatedProjectContributor {
    public CasManagementOverlayReadMeContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./README.md", "classpath:mgmt-overlay/README.md");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        val provider = applicationContext.getBean(InitializrMetadataProvider.class);
        return provider.get().defaults();
    }
}
