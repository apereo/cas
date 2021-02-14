package org.apereo.cas.overlay.casmgmt.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasManagementOverlayLoggingConfigurationContributor extends SingleResourceProjectContributor {

    public CasManagementOverlayLoggingConfigurationContributor() {
        super("./etc/cas/config/log4j2-management.xml",
                "classpath:mgmt-overlay/etc/cas/config/log4j2-management.xml");
    }
}
