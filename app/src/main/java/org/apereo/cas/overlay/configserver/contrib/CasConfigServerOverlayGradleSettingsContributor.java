package org.apereo.cas.overlay.configserver.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasConfigServerOverlayGradleSettingsContributor extends SingleResourceProjectContributor {
    public CasConfigServerOverlayGradleSettingsContributor() {
        super("./settings.gradle", "classpath:configserver-overlay/settings.gradle");
    }
}
