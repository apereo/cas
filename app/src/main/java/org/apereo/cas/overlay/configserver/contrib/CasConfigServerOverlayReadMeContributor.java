package org.apereo.cas.overlay.configserver.contrib;

import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import lombok.val;
import org.apereo.cas.initializr.contrib.TemplatedProjectContributor;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link CasConfigServerOverlayReadMeContributor}.
 *
 * @author Misagh Moayyed
 */
public class CasConfigServerOverlayReadMeContributor extends TemplatedProjectContributor {
    public CasConfigServerOverlayReadMeContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./README.md", "classpath:configserver-overlay/README.md");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        val provider = applicationContext.getBean(InitializrMetadataProvider.class);
        return provider.get().defaults();
    }
}
