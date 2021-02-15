package org.apereo.cas.overlay.discoveryserver.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasDiscoveryServerOverlayGradleSettingsContributor extends SingleResourceProjectContributor {
    public CasDiscoveryServerOverlayGradleSettingsContributor() {
        super("./settings.gradle", "classpath:discoveryserver-overlay/settings.gradle");
    }
}
