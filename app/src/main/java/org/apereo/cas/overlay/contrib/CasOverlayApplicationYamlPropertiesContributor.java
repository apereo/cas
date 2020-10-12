package org.apereo.cas.overlay.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasOverlayApplicationYamlPropertiesContributor extends SingleResourceProjectContributor {
    public CasOverlayApplicationYamlPropertiesContributor() {
        super("src/main/resources/application.yml", "classpath:/overlay/application.yml");
    }

}
