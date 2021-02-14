package org.apereo.cas.overlay.casserver.contrib;

import io.spring.initializr.generator.project.contributor.ProjectContributor;
import lombok.val;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CasOverlayConfigurationDirectoriesContributor implements ProjectContributor {
    private static void createDirectory(final Path projectRoot, final String path) throws IOException {
        val output = projectRoot.resolve(path);
        Files.createDirectories(output);
    }

    @Override
    public void contribute(final Path projectRoot) throws IOException {
        createDirectory(projectRoot, "./etc/cas/services");
        createDirectory(projectRoot, "./etc/cas/saml");
    }
}
