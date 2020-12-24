package org.apereo.cas.initializr.contrib.gradle;

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;

public class GradleWrapperExecutablesContributor extends MultipleResourcesProjectContributor {

    public GradleWrapperExecutablesContributor() {
        super("classpath:common/gradle/exec",
            filename -> filename.equals("gradlew") || filename.equals("gradlew.bat"));
    }
}
