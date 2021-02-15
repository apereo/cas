package org.apereo.cas.overlay.configserver.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

/**
 * This is {@link CasConfigServerOverlayGradleBuildContributor}.
 *
 * @author Misagh Moayyed
 */
public class CasConfigServerOverlayGradleBuildContributor  extends SingleResourceProjectContributor {
    public CasConfigServerOverlayGradleBuildContributor() {
        super("./build.gradle", "classpath:configserver-overlay/build.gradle");
    }
}
