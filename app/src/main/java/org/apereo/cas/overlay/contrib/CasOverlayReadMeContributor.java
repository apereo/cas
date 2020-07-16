package org.apereo.cas.overlay.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayReadMeContributor extends SingleResourceProjectContributor {
    public CasOverlayReadMeContributor(final String relativePath, final String resourcePattern) {
        super(relativePath, resourcePattern);
    }

    public CasOverlayReadMeContributor() {
        this("classpath:overlay/README.md");
    }

    private CasOverlayReadMeContributor(String resourcePattern) {
        super("./README.md", resourcePattern);
    }

}
