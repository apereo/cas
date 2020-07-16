package org.apereo.cas.overlay;

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;

public class CasGradleWrapperContributor extends MultipleResourcesProjectContributor {

    public CasGradleWrapperContributor() {
        super("classpath:gradle/wrapper",
            filename -> filename.equals("gradlew") || filename.equals("gradlew.bat"));
    }

}
