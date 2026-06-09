package org.springframework.boot.gradle.tasks.bundling;

import org.springframework.boot.loader.tools.LibraryCoordinates;
import org.gradle.api.Project;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Isolated-projects-compatible replacement for Spring Boot's dependency mapper.
 *
 * <p>Spring Boot 4.1.0-RC1 builds this map by visiting every project in the
 * build and reading each project's {@code group} and {@code version}. CAS uses a
 * single group and version for all included projects, so project dependency
 * coordinates can be derived from Gradle properties and the dependency's path.
 * @author Misagh Moayyed
 * @see <a href="https://github.com/spring-projects/spring-boot/issues/43755">Spring Boot issue</a>
 * @see <a href="https://github.com/gradle/gradle/issues/31973">Gradle issue</a>
 * @since 8.0.0
 */
public final class ResolvedDependencies {
    private final String projectGroup;

    private final String projectVersion;

    private final ListProperty<ComponentArtifactIdentifier> artifactIds;

    private final ListProperty<File> artifactFiles;

    public ResolvedDependencies(final Project project) {
        this.projectGroup = project.getProviders().gradleProperty("group").get();
        this.projectVersion = project.getProviders().gradleProperty("version").get();
        this.artifactIds = project.getObjects().listProperty(ComponentArtifactIdentifier.class);
        this.artifactFiles = project.getObjects().listProperty(File.class);
    }

    @Input
    public ListProperty<ComponentArtifactIdentifier> getArtifactIds() {
        return this.artifactIds;
    }

    @Classpath
    public ListProperty<File> getArtifactFiles() {
        return this.artifactFiles;
    }

    /**
     * Resolved artifacts.
     *
     * @param resolvedArtifacts the resolved artifacts
     */
    public void resolvedArtifacts(final Provider<Set<ResolvedArtifactResult>> resolvedArtifacts) {
        this.artifactFiles.addAll(
            resolvedArtifacts.map(artifacts -> artifacts.stream().map(ResolvedArtifactResult::getFile).toList()));
        this.artifactIds.addAll(
            resolvedArtifacts.map(artifacts -> artifacts.stream().map(ResolvedArtifactResult::getId).toList()));
    }

    /**
     * Find dependency descriptor.
     *
     * @param file the file
     * @return the dependency descriptor
     */
    public DependencyDescriptor find(final File file) {
        var id = findArtifactIdentifier(file);
        if (id == null) {
            return null;
        }
        var componentIdentifier = id.getComponentIdentifier();
        if (componentIdentifier instanceof ModuleComponentIdentifier moduleId) {
            return new DependencyDescriptor(
                LibraryCoordinates.of(moduleId.getGroup(), moduleId.getModule(), moduleId.getVersion()), false);
        }
        if (componentIdentifier instanceof ProjectComponentIdentifier projectId) {
            return new DependencyDescriptor(
                LibraryCoordinates.of(this.projectGroup, projectName(projectId.getProjectPath()), this.projectVersion),
                true);
        }
        return null;
    }

    private ComponentArtifactIdentifier findArtifactIdentifier(final File file) {
        List<File> files = this.artifactFiles.get();
        for (var i = 0; i < files.size(); i++) {
            if (file.equals(files.get(i))) {
                return this.artifactIds.get().get(i);
            }
        }
        return null;
    }

    private static String projectName(final String projectPath) {
        var separatorIndex = projectPath.lastIndexOf(':');
        return separatorIndex >= 0 ? projectPath.substring(separatorIndex + 1) : projectPath;
    }

    /**
     * Describes a dependency in an executable archive.
     */
    public static final class DependencyDescriptor {
        private final LibraryCoordinates coordinates;

        private final boolean projectDependency;

        public DependencyDescriptor(final LibraryCoordinates coordinates, final boolean projectDependency) {
            this.coordinates = Objects.requireNonNull(coordinates);
            this.projectDependency = projectDependency;
        }

        public LibraryCoordinates getCoordinates() {
            return this.coordinates;
        }

        public boolean isProjectDependency() {
            return this.projectDependency;
        }
    }
}
