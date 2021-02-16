package org.apereo.cas.overlay.bootadminserver.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

/**
 * This is {@link CasSpringBootAdminServerOverlayGradleBuildContributor}.
 *
 * @author Misagh Moayyed
 */
public class CasSpringBootAdminServerOverlayGradleBuildContributor extends SingleResourceProjectContributor {
    public CasSpringBootAdminServerOverlayGradleBuildContributor() {
        super("./build.gradle", "classpath:bootadmin-overlay/build.gradle");
    }
}
