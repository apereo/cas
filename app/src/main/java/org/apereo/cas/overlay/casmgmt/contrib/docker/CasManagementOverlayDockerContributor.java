package org.apereo.cas.overlay.casmgmt.contrib.docker;

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;

/**
 * This is {@link CasManagementOverlayDockerContributor}.
 *
 * @author Misagh Moayyed
 */
public class CasManagementOverlayDockerContributor extends MultipleResourcesProjectContributor {

    public CasManagementOverlayDockerContributor() {
        super("classpath:mgmt-overlay/docker", filename -> filename.endsWith(".sh"));
    }

}

