package org.apereo.cas.initializr.contrib.gradle;

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;

import java.io.IOException;
import java.nio.file.Path;

public class GradleWrapperConfigurationContributor extends MultipleResourcesProjectContributor {

    public GradleWrapperConfigurationContributor() {
        super("classpath:common/gradle/wrapper",
            filename -> filename.equals("gradlew") || filename.equals("gradlew.bat"));
    }

    @Override
    public void contribute(final Path projectRoot) throws IOException {
        var root = projectRoot.resolve("gradle/wrapper");
        super.contribute(root);
    }
}
