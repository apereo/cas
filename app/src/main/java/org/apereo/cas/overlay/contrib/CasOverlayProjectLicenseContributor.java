package org.apereo.cas.overlay.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayProjectLicenseContributor extends SingleResourceProjectContributor {
    public CasOverlayProjectLicenseContributor() {
        super("./LICENSE.txt", "classpath:overlay/LICENSE.txt");
    }

}
