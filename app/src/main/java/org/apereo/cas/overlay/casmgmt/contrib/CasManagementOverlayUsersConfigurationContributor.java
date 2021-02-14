package org.apereo.cas.overlay.casmgmt.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasManagementOverlayUsersConfigurationContributor extends SingleResourceProjectContributor {

    public CasManagementOverlayUsersConfigurationContributor() {
        super("./etc/cas/config/users.json",
                "classpath:mgmt-overlay/etc/cas/config/users.json");
    }
}
