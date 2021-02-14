package org.apereo.cas.overlay.casserver.contrib.gradle;

import org.apereo.cas.overlay.casserver.buildsystem.CasOverlayGradleBuild;
import org.apereo.cas.initializr.contrib.TemplatedProjectContributor;
import io.spring.initializr.generator.project.ProjectDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CasOverlayGradleBuildContributor extends TemplatedProjectContributor {
    public CasOverlayGradleBuildContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./build.gradle", "classpath:overlay/build.gradle");
    }

    @Override
    public Object contributeInternal(final ProjectDescription project) {
        val dependencies = project.getRequestedDependencies()
            .values()
            .stream()
            .filter(dep -> !CasOverlayGradleBuild.WEBAPP_ARTIFACTS.contains(dep.getArtifactId()))
            .map(dep -> new CasDependency(dep.getGroupId(), dep.getArtifactId()))
            .collect(Collectors.toList());
        log.debug("Requested dependencies: {}", dependencies);
        return new Dependencies(dependencies);
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
