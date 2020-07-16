package org.apereo.cas.overlay.contrib.gradle;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayGradleSpringBootContributor extends SingleResourceProjectContributor {

    public CasOverlayGradleSpringBootContributor() {
        this("classpath:overlay/gradle/springboot.gradle");
    }

    private CasOverlayGradleSpringBootContributor(String resourcePattern) {
        super("gradle/springboot.gradle", resourcePattern);
    }
}
