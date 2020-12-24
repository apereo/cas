package org.apereo.cas.initializr.contrib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

public class ProjectLicenseContributor extends SingleResourceProjectContributor {
    public ProjectLicenseContributor() {
        super("./LICENSE.txt", "classpath:common/LICENSE.txt");
    }

}
