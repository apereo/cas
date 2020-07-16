package org.apereo.cas.overlay.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayProjectLicenseContributor extends SingleResourceProjectContributor {
    public CasOverlayProjectLicenseContributor(final String relativePath, final String resourcePattern) {
        super(relativePath, resourcePattern);
    }

    public CasOverlayProjectLicenseContributor() {
        this("classpath:overlay/LICENSE.txt");
    }

    private CasOverlayProjectLicenseContributor(String resourcePattern) {
        super("./LICENSE.txt", resourcePattern);
    }

}
