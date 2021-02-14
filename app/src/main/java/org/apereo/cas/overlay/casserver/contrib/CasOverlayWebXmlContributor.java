package org.apereo.cas.overlay.casserver.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayWebXmlContributor extends SingleResourceProjectContributor {
    public CasOverlayWebXmlContributor() {
        super("src/main/webapp/WEB-INF/web.xml", "classpath:overlay/webapp/web.xml");
    }
}
