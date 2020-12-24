package org.apereo.cas.initializr.contrib;


import io.spring.initializr.generator.project.contributor.ProjectContributor;
import org.springframework.core.Ordered;
import org.springframework.util.FileSystemUtils;
import java.io.IOException;
import java.nio.file.Path;

/**
 * There is no way to disable contributors from the initializr.
 * This component attempts to undo "damage" done by the core framework
 * until we have a way to disable contributors that are not needed.
 */
public class ProjectAssetsUndoContributor implements ProjectContributor {
    private static void delete(final Path projectRoot, final String path) throws IOException {
        FileSystemUtils.deleteRecursively(projectRoot.resolve(path));
    }

    @Override
    public void contribute(final Path projectRoot) throws IOException {
        delete(projectRoot, "src/test");
        delete(projectRoot, "src/main/resources/application.properties");
        delete(projectRoot, "src/main/java/org/apereo/cas/cas");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
