package org.apereo.cas.overlay.contrib.docker.jib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

import java.io.IOException;
import java.nio.file.Path;

public class CasOverlayGradleJibEntrypointContributor extends SingleResourceProjectContributor {
    public CasOverlayGradleJibEntrypointContributor() {
        this("classpath:overlay/jib/entrypoint.sh");
    }

    private CasOverlayGradleJibEntrypointContributor(String resourcePattern) {
        super("src/main/jib/docker/entrypoint.sh", resourcePattern);
    }

    @Override
    public void contribute(final Path projectRoot) throws IOException {
        super.contribute(projectRoot);
        var output = projectRoot.resolve("src/main/jib/docker/entrypoint.sh");
        output.toFile().setExecutable(true);
    }
}
