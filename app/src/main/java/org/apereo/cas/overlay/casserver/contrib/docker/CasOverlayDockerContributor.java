package org.apereo.cas.overlay.casserver.contrib.docker;

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;

public class CasOverlayDockerContributor extends MultipleResourcesProjectContributor {

    public CasOverlayDockerContributor() {
        super("classpath:overlay/docker", filename -> filename.endsWith(".sh"));
    }

}
