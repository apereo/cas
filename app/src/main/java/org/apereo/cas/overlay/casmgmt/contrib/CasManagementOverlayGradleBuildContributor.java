package org.apereo.cas.overlay.casmgmt.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasManagementOverlayGradleBuildContributor extends SingleResourceProjectContributor {
    public CasManagementOverlayGradleBuildContributor() {
        super("./build.gradle", "classpath:mgmt-overlay/build.gradle");
    }
}
