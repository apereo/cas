package org.apereo.cas.overlay.bootadminserver.contrib.docker.jib;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This is {@link CasSpringBootAdminGradleJibEntrypointContributor}.
 *
 * @author Misagh Moayyed
 */
public class CasSpringBootAdminGradleJibEntrypointContributor extends SingleResourceProjectContributor {
    public CasSpringBootAdminGradleJibEntrypointContributor() {
        this("classpath:bootadmin-overlay/jib/entrypoint.sh");
    }

    private CasSpringBootAdminGradleJibEntrypointContributor(String resourcePattern) {
        super("src/main/jib/docker/entrypoint.sh", resourcePattern);
    }

    @Override
    public void contribute(final Path projectRoot) throws IOException {
        super.contribute(projectRoot);
        var output = projectRoot.resolve("src/main/jib/docker/entrypoint.sh");
        output.toFile().setExecutable(true);
    }
}

