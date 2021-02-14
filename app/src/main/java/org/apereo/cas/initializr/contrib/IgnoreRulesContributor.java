package org.apereo.cas.initializr.contrib;

import io.spring.initializr.generator.project.contributor.ProjectContributor;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class IgnoreRulesContributor implements ProjectContributor {
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Override
    public void contribute(final Path projectRoot) throws IOException {
        createFile(projectRoot, ".gitignore", "classpath:common/gitignore");
        createFile(projectRoot, ".dockerignore", "classpath:common/dockerignore");
        createFile(projectRoot, ".gitattributes", "classpath:common/gitattributes");
    }

    private void createFile(final Path projectRoot, final String relativePath,
                            final String resourcePattern) throws IOException {
        Path output = projectRoot.resolve(relativePath);
        if (!Files.exists(output)) {
            Files.createDirectories(output.getParent());
            Files.createFile(output);
        }
        Resource resource = this.resolver.getResource(resourcePattern);
        FileCopyUtils.copy(resource.getInputStream(), Files.newOutputStream(output, StandardOpenOption.APPEND));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
