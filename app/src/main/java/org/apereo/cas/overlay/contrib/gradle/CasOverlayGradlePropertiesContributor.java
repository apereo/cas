package org.apereo.cas.overlay.contrib.gradle;

import com.github.mustachejava.DefaultMustacheFactory;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
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
public class CasOverlayGradlePropertiesContributor implements ProjectContributor {
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private final ApplicationContext applicationContext;
    
    @Override
    public void contribute(final Path projectRoot) throws IOException {
        var output = projectRoot.resolve("./gradle.properties");
        if (!Files.exists(output)) {
            Files.createDirectories(output.getParent());
            Files.createFile(output);
        }
        Resource resource = resolver.getResource("classpath:overlay/gradle.properties");
        val mf = new DefaultMustacheFactory();
        val mustache = mf.compile(new InputStreamReader(resource.getInputStream()), resource.getFilename());
        try (val writer = new StringWriter()) {
            val project = applicationContext.getBean(ProjectDescription.class);
            val dependencies = project.getRequestedDependencies();
            var appServer = "-tomcat";
            if (dependencies.containsKey("webapp-jetty")) {
                appServer = "-jetty";
            } else if (dependencies.containsKey("webapp-undertow")) {
                appServer = "-undertow";
            }
            mustache.execute(writer, new CasPropertiesContainer(new CasProperties(appServer))).flush();
            val template = writer.toString();
            FileCopyUtils.copy(new BufferedInputStream(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8))),
                Files.newOutputStream(output, StandardOpenOption.APPEND));
        }
    }

    @RequiredArgsConstructor
    private static class CasPropertiesContainer {
        private final CasProperties properties;

        CasProperties properties() {
            return this.properties;
        }
    }

    @Getter
    @AllArgsConstructor
    @ToString
    private static class CasProperties {
        private final String appServer;
    }
}
