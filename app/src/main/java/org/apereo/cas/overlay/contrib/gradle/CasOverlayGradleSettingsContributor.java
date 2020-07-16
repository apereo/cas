package org.apereo.cas.overlay.contrib.gradle;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayGradleSettingsContributor extends SingleResourceProjectContributor {

    public CasOverlayGradleSettingsContributor() {
        this("classpath:overlay/settings.gradle");
    }

    private CasOverlayGradleSettingsContributor(String resourcePattern) {
        super("./settings.gradle", resourcePattern);
    }
}
