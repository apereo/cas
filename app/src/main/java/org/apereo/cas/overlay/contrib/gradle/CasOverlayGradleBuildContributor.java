package org.apereo.cas.overlay.contrib.gradle;

import org.apereo.cas.overlay.CasOverlayGradleBuild;
import com.github.mustachejava.DefaultMustacheFactory;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@RequiredArgsConstructor
@Slf4j
public class CasOverlayGradleBuildContributor implements ProjectContributor {
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private final ApplicationContext applicationContext;

    @Override
    public void contribute(final Path projectRoot) throws IOException {
        var output = projectRoot.resolve("./build.gradle");
        if (!Files.exists(output)) {
            Files.createDirectories(output.getParent());
            Files.createFile(output);
        }
        val resource = resolver.getResource("classpath:overlay/build.gradle");
        val mf = new DefaultMustacheFactory();
        val mustache = mf.compile(new InputStreamReader(resource.getInputStream()), resource.getFilename());
        try (val writer = new StringWriter()) {
            val project = applicationContext.getBean(ProjectDescription.class);
            val dependencies = project.getRequestedDependencies()
                .values()
                .stream()
                .filter(dep -> !CasOverlayGradleBuild.WEBAPP_ARTIFACTS.contains(dep.getArtifactId()))
                .map(dep -> new CasDependency(dep.getGroupId(), dep.getArtifactId()))
                .collect(Collectors.toList());
            log.debug("Requested dependencies: {}", dependencies);
            mustache.execute(writer, new Dependencies(dependencies)).flush();
            val template = writer.toString();
            log.trace("Rendered dependencies in the build:\n{}", template);
            FileCopyUtils.copy(new BufferedInputStream(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8))),
                Files.newOutputStream(output, StandardOpenOption.APPEND));
        }
    }

    @RequiredArgsConstructor
    private static class Dependencies {
        private final List<CasDependency> dependencies;

        List<CasDependency> dependencies() {
            return this.dependencies;
        }
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class CasDependency {
        private final String groupId;

        private final String artifactId;
    }
}
