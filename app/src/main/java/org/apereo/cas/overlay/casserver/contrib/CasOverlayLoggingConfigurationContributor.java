package org.apereo.cas.overlay.casserver.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayLoggingConfigurationContributor extends SingleResourceProjectContributor {

    public CasOverlayLoggingConfigurationContributor() {
        super("./etc/cas/config/log4j2.xml", "classpath:overlay/etc/cas/config/log4j2.xml");
    }
}
