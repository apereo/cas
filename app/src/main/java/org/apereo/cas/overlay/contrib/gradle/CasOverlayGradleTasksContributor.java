package org.apereo.cas.overlay.contrib.gradle;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayGradleTasksContributor extends SingleResourceProjectContributor {

    public CasOverlayGradleTasksContributor() {
        super("gradle/tasks.gradle", "classpath:overlay/gradle/tasks.gradle");
    }
}
