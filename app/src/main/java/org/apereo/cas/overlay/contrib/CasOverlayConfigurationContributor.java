package org.apereo.cas.overlay.contrib;

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;

import java.io.IOException;
import java.nio.file.Path;

public class CasOverlayConfigurationContributor extends MultipleResourcesProjectContributor {

    public CasOverlayConfigurationContributor() {
        super("classpath:overlay/etc/cas");
    }

    @Override
    public void contribute(final Path projectRoot) throws IOException {
        var root = projectRoot.resolve("etc/cas");
        super.contribute(root);
    }
}
