package org.apereo.cas.overlay.contrib.gradle.wrapper;

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;

import java.io.IOException;
import java.nio.file.Path;

public class CasOverlayGradleWrapperConfigurationContributor extends MultipleResourcesProjectContributor {

    public CasOverlayGradleWrapperConfigurationContributor() {
        super("classpath:overlay/gradle/wrapper",
            filename -> filename.equals("gradlew") || filename.equals("gradlew.bat"));
    }

    @Override
    public void contribute(final Path projectRoot) throws IOException {
        var root = projectRoot.resolve("gradle/wrapper");
        super.contribute(root);
    }
}
