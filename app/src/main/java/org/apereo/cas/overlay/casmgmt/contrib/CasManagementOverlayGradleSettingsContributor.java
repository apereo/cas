package org.apereo.cas.overlay.casmgmt.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasManagementOverlayGradleSettingsContributor extends SingleResourceProjectContributor {
    public CasManagementOverlayGradleSettingsContributor() {
        super("./settings.gradle", "classpath:mgmt-overlay/settings.gradle");
    }
}
