package org.apereo.cas.overlay.contrib.util;

import com.github.mustachejava.DefaultMustacheFactory;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Slf4j
public abstract class TemplatedProjectContributor implements ProjectContributor {
    protected final ApplicationContext applicationContext;

    protected final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private final String relativePath;

    private final String resourcePattern;

    @Override
    public void contribute(final Path projectRoot) throws IOException {
        val output = projectRoot.resolve(relativePath);
        if (!Files.exists(output)) {
            Files.createDirectories(output.getParent());
            Files.createFile(output);
        }
        val resource = resolver.getResource(resourcePattern);
        val mf = new DefaultMustacheFactory();
        val mustache = mf.compile(new InputStreamReader(resource.getInputStream()), resource.getFilename());
        try (val writer = new StringWriter()) {
            val project = applicationContext.getBean(ProjectDescription.class);
            mustache.execute(writer, contributeInternal(project)).flush();
            val template = writer.toString();
            FileCopyUtils.copy(new BufferedInputStream(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8))),
                Files.newOutputStream(output, StandardOpenOption.APPEND));
        }
    }

    protected abstract Object contributeInternal(ProjectDescription project);
}
