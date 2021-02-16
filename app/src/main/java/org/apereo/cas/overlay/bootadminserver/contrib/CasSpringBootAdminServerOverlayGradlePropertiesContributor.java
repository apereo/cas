package org.apereo.cas.overlay.bootadminserver.contrib;

import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import lombok.val;
import org.apereo.cas.initializr.contrib.TemplatedProjectContributor;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link CasSpringBootAdminServerOverlayGradlePropertiesContributor}.
 *
 * @author Misagh Moayyed
 */
public class CasSpringBootAdminServerOverlayGradlePropertiesContributor extends TemplatedProjectContributor {
    public CasSpringBootAdminServerOverlayGradlePropertiesContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./gradle.properties", "classpath:bootadmin-overlay/gradle.properties");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        val provider = applicationContext.getBean(InitializrMetadataProvider.class);
        val defaults = provider.get().defaults();
        defaults.put("casVersion", provider.get().getConfiguration().getEnv().getBoms().get("cas-bom").getVersion());
        return defaults;
    }
}

