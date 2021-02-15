package org.apereo.cas.overlay.discoveryserver.contrib;

import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import lombok.val;
import org.apereo.cas.initializr.contrib.TemplatedProjectContributor;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link CasDiscoveryServerOverlayReadMeContributor}.
 *
 * @author Misagh Moayyed
 */
public class CasDiscoveryServerOverlayReadMeContributor extends TemplatedProjectContributor {
    public CasDiscoveryServerOverlayReadMeContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./README.md", "classpath:discoveryserver-overlay/README.md");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        val provider = applicationContext.getBean(InitializrMetadataProvider.class);
        return provider.get().defaults();
    }
}
