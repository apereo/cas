package org.apereo.cas.overlay.discoveryserver.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

/**
 * This is {@link CasDiscoveryServerOverlayGradleBuildContributor}.
 *
 * @author Misagh Moayyed
 */
public class CasDiscoveryServerOverlayGradleBuildContributor extends SingleResourceProjectContributor {
    public CasDiscoveryServerOverlayGradleBuildContributor() {
        super("./build.gradle", "classpath:discoveryserver-overlay/build.gradle");
    }
}
