package org.apereo.cas.web.report;

import module java.base;
import lombok.val;
import org.jooq.lambda.fi.lang.CheckedRunnable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DependenciesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@TestPropertySource(properties = "management.endpoint.dependencies.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@Execution(ExecutionMode.SAME_THREAD)
class DependenciesEndpointTests extends AbstractCasEndpointTests {
    private static final String JAVA_CLASS_PATH = "java.class.path";

    @Test
    void verifyDependencies(@TempDir final Path directory) throws Throwable {
        val jar = createDependencyJar(directory, "org.apereo.cas", "cas-server-test", "1.0.0");
        withRuntimeClasspath(jar.toString(), () -> mockMvc.perform(get("/actuator/dependencies")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].groupId").value("org.apereo.cas"))
            .andExpect(jsonPath("$[0].artifactId").value("cas-server-test"))
            .andExpect(jsonPath("$[0].version").value("1.0.0"))
            .andExpect(jsonPath("$[0].source").value("cas-server-test-1.0.0.jar")));
    }

    @Test
    void verifyVulnerabilitiesWithNoDependencies() throws Throwable {
        withRuntimeClasspath(String.valueOf(Path.of("missing-runtime-classpath-entry.jar")),
            () -> mockMvc.perform(get("/actuator/dependencies/vulnerabilities")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dependencyCount").value(0))
                .andExpect(jsonPath("$.vulnerabilitiesCount").value(0))
                .andExpect(jsonPath("$.vulnerabilities").doesNotExist())
                .andExpect(jsonPath("$.errors").doesNotExist()));
    }

    private static Path createDependencyJar(final Path directory,
                                            final String groupId,
                                            final String artifactId,
                                            final String version) throws IOException {
        val jar = directory.resolve(artifactId + '-' + version + ".jar");
        try (val output = new JarOutputStream(Files.newOutputStream(jar))) {
            val entry = new JarEntry("META-INF/maven/%s/%s/pom.properties".formatted(groupId, artifactId));
            output.putNextEntry(entry);
            val properties = """
                groupId=%s
                artifactId=%s
                version=%s
                """.formatted(groupId, artifactId, version);
            output.write(properties.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return jar;
    }

    private static void withRuntimeClasspath(final String classpath,
                                             final CheckedRunnable runnable) throws Throwable {
        val originalClasspath = System.getProperty(JAVA_CLASS_PATH);
        val originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            System.setProperty(JAVA_CLASS_PATH, classpath);
            Thread.currentThread().setContextClassLoader(new ClassLoader(null) {
            });
            runnable.run();
        } finally {
            if (originalClasspath == null) {
                System.clearProperty(JAVA_CLASS_PATH);
            } else {
                System.setProperty(JAVA_CLASS_PATH, originalClasspath);
            }
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
}
