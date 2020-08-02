package org.apereo.cas.overlay.contrib.spring;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class CasApplicationYamlPropertiesContributor extends SingleResourceProjectContributor {
    public CasApplicationYamlPropertiesContributor() {
        this("classpath:/overlay/application.yml");
    }

    public CasApplicationYamlPropertiesContributor(String resourcePattern) {
        super("src/main/resources/application.yml", resourcePattern);
    }

}
