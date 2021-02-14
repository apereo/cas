package org.apereo.cas.initializr.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class OverlayOverrideConfigurationContributor extends SingleResourceProjectContributor {
    public static final String PACKAGE_NAME_SRC_CAS = "src/main/java/org/apereo/cas";

    public static final String RESOURCE_PATH_CONFIGURATION_CLASS = PACKAGE_NAME_SRC_CAS + "/config/CasOverlayOverrideConfiguration.java";

    public OverlayOverrideConfigurationContributor() {
        super("./" + RESOURCE_PATH_CONFIGURATION_CLASS, "classpath:common/" + RESOURCE_PATH_CONFIGURATION_CLASS);
    }
}
