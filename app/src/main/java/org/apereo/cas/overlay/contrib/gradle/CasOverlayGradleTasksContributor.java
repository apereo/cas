package org.apereo.cas.overlay.contrib.gradle;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayGradleTasksContributor extends SingleResourceProjectContributor {

    public CasOverlayGradleTasksContributor() {
        this("classpath:overlay/gradle/tasks.gradle");
    }

    private CasOverlayGradleTasksContributor(String resourcePattern) {
        super("gradle/tasks.gradle", resourcePattern);
    }
}
