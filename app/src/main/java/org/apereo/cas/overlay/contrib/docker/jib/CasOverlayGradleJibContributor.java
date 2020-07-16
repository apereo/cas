package org.apereo.cas.overlay.contrib.docker.jib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayGradleJibContributor extends SingleResourceProjectContributor {
    public CasOverlayGradleJibContributor() {
        this("classpath:overlay/jib/dockerjib.gradle");
    }

    private CasOverlayGradleJibContributor(String resourcePattern) {
        super("gradle/dockerjib.gradle", resourcePattern);
    }

}
