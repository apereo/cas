package org.apereo.cas.overlay.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasApplicationYamlPropertiesContributor extends SingleResourceProjectContributor {
    public CasApplicationYamlPropertiesContributor() {
        super("src/main/resources/application.yml", "classpath:/overlay/application.yml");
    }

}
