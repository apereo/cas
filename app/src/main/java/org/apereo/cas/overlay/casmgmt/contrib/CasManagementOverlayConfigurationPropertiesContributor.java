package org.apereo.cas.overlay.casmgmt.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;
import org.springframework.context.ApplicationContext;

public class CasManagementOverlayConfigurationPropertiesContributor extends SingleResourceProjectContributor {

    public CasManagementOverlayConfigurationPropertiesContributor(final ApplicationContext applicationContext) {
        super("./etc/cas/config/management.properties",
                "classpath:/mgmt-overlay/etc/cas/config/management.properties");
    }
}
