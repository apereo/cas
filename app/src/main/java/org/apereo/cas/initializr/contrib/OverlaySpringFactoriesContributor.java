package org.apereo.cas.initializr.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;


public class OverlaySpringFactoriesContributor extends SingleResourceProjectContributor {
    public static final String PACKAGE_NAME_SRC_RESOURCES = "src/main/resources";

    public static final String RESOURCE_PATH_SPRING_FACTORIES = PACKAGE_NAME_SRC_RESOURCES + "/META-INF/spring.factories";

    public OverlaySpringFactoriesContributor() {
        super("./" + RESOURCE_PATH_SPRING_FACTORIES,
            "classpath:common/" + RESOURCE_PATH_SPRING_FACTORIES);
    }
}
