package org.apereo.cas.overlay.casserver.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;


public class CasOverlaySpringFactoriesContributor extends SingleResourceProjectContributor {
    public static final String PACKAGE_NAME_SRC_RESOURCES = "src/main/resources";

    public static final String RESOURCE_PATH_SPRING_FACTORIES = PACKAGE_NAME_SRC_RESOURCES + "/META-INF/spring.factories";

    public CasOverlaySpringFactoriesContributor() {
        super("./" + RESOURCE_PATH_SPRING_FACTORIES,
            "classpath:overlay/" + RESOURCE_PATH_SPRING_FACTORIES);
    }
}
