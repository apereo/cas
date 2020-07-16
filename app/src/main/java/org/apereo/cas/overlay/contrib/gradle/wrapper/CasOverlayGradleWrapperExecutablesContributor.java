package org.apereo.cas.overlay.contrib.gradle.wrapper;

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;

public class CasOverlayGradleWrapperExecutablesContributor extends MultipleResourcesProjectContributor {

    public CasOverlayGradleWrapperExecutablesContributor() {
        super("classpath:overlay/gradle/exec",
            filename -> filename.equals("gradlew") || filename.equals("gradlew.bat"));
    }
}
