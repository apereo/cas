package org.apereo.cas.initializr.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class ApplicationYamlPropertiesContributor extends SingleResourceProjectContributor {
    public ApplicationYamlPropertiesContributor() {
        super("src/main/resources/application.yml", "classpath:/common/application.yml");
    }

}
