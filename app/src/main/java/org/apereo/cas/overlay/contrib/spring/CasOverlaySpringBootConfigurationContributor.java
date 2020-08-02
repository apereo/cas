package org.apereo.cas.overlay.contrib.spring;

import io.spring.initializr.generator.project.contributor.ProjectContributor;
import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This is {@link CasOverlaySpringBootConfigurationContributor}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CasOverlaySpringBootConfigurationContributor implements ProjectContributor {
    public static final String PACKAGE_NAME_SRC_CAS = "src/main/java/org/apereo/cas";

    public static final String PACKAGE_NAME_SRC_RESOURCES = "src/main/resources";

    public static final String RESOURCE_PATH_CONFIGURATION_CLASS = PACKAGE_NAME_SRC_CAS + "/config/CasOverlayOverrideConfiguration.java";

    public static final String RESOURCE_PATH_SPRING_FACTORIES = PACKAGE_NAME_SRC_RESOURCES + "/META-INF/spring.factories";

    @Override
    public void contribute(Path projectRoot) throws IOException {
        new SingleResourceProjectContributor("./" + RESOURCE_PATH_CONFIGURATION_CLASS,
            "classpath:overlay/" + RESOURCE_PATH_CONFIGURATION_CLASS)
            .contribute(projectRoot);

        new SingleResourceProjectContributor("./" + RESOURCE_PATH_SPRING_FACTORIES,
            "classpath:overlay/" + RESOURCE_PATH_SPRING_FACTORIES)
            .contribute(projectRoot);
    }

}
