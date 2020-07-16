package org.apereo.cas.overlay.contrib.gradle;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayGradleBuildContributor extends SingleResourceProjectContributor {

    public CasOverlayGradleBuildContributor() {
        this("classpath:overlay/build.gradle");
    }

    private CasOverlayGradleBuildContributor(String resourcePattern) {
        super("./build.gradle", resourcePattern);
    }
}
