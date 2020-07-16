package org.apereo.cas.overlay.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayWebXmlContributor extends SingleResourceProjectContributor {
    public CasOverlayWebXmlContributor(final String relativePath, final String resourcePattern) {
        super(relativePath, resourcePattern);
    }

    public CasOverlayWebXmlContributor() {
        this("classpath:overlay/webapp/web.xml");
    }

    private CasOverlayWebXmlContributor(String resourcePattern) {
        super("src/main/webapp/WEB-INF/web.xml", resourcePattern);
    }

}
