package org.apereo.cas.overlay.casserver.contrib.gradle;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayGradleSpringBootContributor extends SingleResourceProjectContributor {

    public CasOverlayGradleSpringBootContributor() {
        super("gradle/springboot.gradle", "classpath:overlay/gradle/springboot.gradle");
    }
}
