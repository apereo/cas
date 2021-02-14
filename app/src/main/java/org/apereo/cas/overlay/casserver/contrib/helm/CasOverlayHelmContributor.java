package org.apereo.cas.overlay.casserver.contrib.helm;

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;

public class CasOverlayHelmContributor extends MultipleResourcesProjectContributor {

    public CasOverlayHelmContributor() {
        super("classpath:overlay/helmcharts/", filename -> filename.endsWith(".sh"));
    }

}
