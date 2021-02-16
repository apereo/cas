package org.apereo.cas.overlay.bootadminserver.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

/**
 * This is {@link CasSpringBootAdminServerOverlayGradleSettingsContributor}.
 *
 * @author Misagh Moayyed
 */
public class CasSpringBootAdminServerOverlayGradleSettingsContributor extends SingleResourceProjectContributor {
    public CasSpringBootAdminServerOverlayGradleSettingsContributor() {
        super("./settings.gradle", "classpath:bootadmin-overlay/settings.gradle");
    }
}
