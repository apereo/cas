package org.apereo.cas.overlay.contrib.gradle;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayGradlePropertiesContributor extends SingleResourceProjectContributor {

    public CasOverlayGradlePropertiesContributor() {
        this("classpath:overlay/gradle.properties");
    }

    private CasOverlayGradlePropertiesContributor(String resourcePattern) {
        super("./gradle.properties", resourcePattern);
    }
}
