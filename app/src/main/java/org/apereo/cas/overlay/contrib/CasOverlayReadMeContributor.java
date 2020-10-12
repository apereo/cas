package org.apereo.cas.overlay.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayReadMeContributor extends SingleResourceProjectContributor {
    public CasOverlayReadMeContributor() {
        super("./README.md", "classpath:overlay/README.md");
    }
}
