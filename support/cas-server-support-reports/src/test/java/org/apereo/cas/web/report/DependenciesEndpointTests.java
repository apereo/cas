package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import com.jayway.jsonpath.JsonPath;
import lombok.val;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.jooq.lambda.fi.lang.CheckedRunnable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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
@ResourceLock(Resources.SYSTEM_PROPERTIES)
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

    /**
     * OSV pages each query of a batch on its own: only the query whose result carries a {@code next_page_token} is sent
     * again, with that token as its {@code page_token}. The report counts vulnerable dependencies, not findings.
     */
    @Test
    void verifyVulnerabilitiesAndPagination(@TempDir final Path directory) throws Throwable {
        val paginated = createDependencyJar(directory, "org.apereo.cas", "cas-server-test", "1.0.0");
        val single = createDependencyJar(directory, "org.apereo.cas", "cas-server-other", "2.0.0");
        try (val firstBatch = response(200, """
            {"results":[{"vulns":[{"id":"CVE-TEST-1"}],"next_page_token":"next-page"},{"vulns":[{"id":"CVE-OTHER-1"}]}]}
            """);
             val firstDetails = response(200, """
                 {"id":"CVE-TEST-1","summary":"First finding","aliases":["TEST-1"]}
                 """);
             val otherDetails = response(200, """
                 {"id":"CVE-OTHER-1","summary":"Other finding"}
                 """);
             val secondBatch = response(200, """
                 {"results":[{"vulns":[{"id":"CVE-TEST-2"}]}]}
                 """);
             val secondDetails = response(200, """
                 {"id":"CVE-TEST-2","summary":"Second finding"}
                 """);
             val http = mockStatic(HttpUtils.class)) {
            http.when(() -> HttpUtils.execute(any(HttpExecutionRequest.class)))
                .thenReturn(firstBatch, firstDetails, otherDetails, secondBatch, secondDetails);
            withRuntimeClasspath(paginated + File.pathSeparator + single, () -> mockMvc.perform(get("/actuator/dependencies/vulnerabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dependencyCount").value(2))
                .andExpect(jsonPath("$.vulnerabilitiesCount").value(2))
                .andExpect(jsonPath("$.vulnerabilities[*].details.id").value(contains("CVE-TEST-1", "CVE-OTHER-1", "CVE-TEST-2")))
                .andExpect(jsonPath("$.vulnerabilities[0].dependency.groupId").value("org.apereo.cas"))
                .andExpect(jsonPath("$.vulnerabilities[0].dependency.artifactId").value("cas-server-test"))
                .andExpect(jsonPath("$.vulnerabilities[1].dependency.artifactId").value("cas-server-other"))
                .andExpect(jsonPath("$.vulnerabilities[2].dependency.artifactId").value("cas-server-test"))
                .andExpect(jsonPath("$.errors").doesNotExist()));
            val requests = ArgumentCaptor.forClass(HttpExecutionRequest.class);
            http.verify(() -> HttpUtils.execute(requests.capture()), times(5));
            val captured = requests.getAllValues();
            val firstRequest = captured.getFirst();
            assertEquals(HttpMethod.POST, firstRequest.getMethod());
            assertEquals("https://api.osv.dev/v1/querybatch", firstRequest.getUrl());
            val firstBody = JsonPath.parse(firstRequest.getEntity());
            assertEquals(List.of("org.apereo.cas:cas-server-test", "org.apereo.cas:cas-server-other"),
                firstBody.read("$.queries[*].package.name", List.class));
            assertEquals("Maven", firstBody.read("$.queries[0].package.ecosystem", String.class));
            assertEquals("1.0.0", firstBody.read("$.queries[0].version", String.class));
            assertFalse(firstRequest.getEntity().contains("page_token"));
            assertEquals(HttpMethod.GET, captured.get(1).getMethod());
            assertEquals("https://api.osv.dev/v1/vulns/CVE-TEST-1", captured.get(1).getUrl());
            assertEquals("https://api.osv.dev/v1/vulns/CVE-OTHER-1", captured.get(2).getUrl());
            val secondBody = JsonPath.parse(captured.get(3).getEntity());
            assertEquals(List.of("org.apereo.cas:cas-server-test"), secondBody.read("$.queries[*].package.name", List.class));
            assertEquals("next-page", secondBody.read("$.queries[0].page_token", String.class));
            assertEquals("https://api.osv.dev/v1/vulns/CVE-TEST-2", captured.get(4).getUrl());
            List.of(firstBatch, firstDetails, otherDetails, secondBatch, secondDetails)
                .forEach(response -> http.verify(() -> HttpUtils.close(response)));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"results\":[]}", "{\"results\":[null]}", "{\"results\":[{}]}", "{\"results\":[{\"vulns\":null}]}"})
    void verifyMissingVulnerabilityResults(final String body, @TempDir final Path directory) throws Throwable {
        val jar = createDependencyJar(directory, "org.apereo.cas", "cas-server-test", "1.0.0");
        try (val batch = response(200, body);
             val http = mockStatic(HttpUtils.class)) {
            http.when(() -> HttpUtils.execute(any(HttpExecutionRequest.class))).thenReturn(batch);
            withRuntimeClasspath(jar.toString(), () -> mockMvc.perform(get("/actuator/dependencies/vulnerabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dependencyCount").value(1))
                .andExpect(jsonPath("$.vulnerabilitiesCount").value(0))
                .andExpect(jsonPath("$.vulnerabilities").doesNotExist())
                .andExpect(jsonPath("$.errors").doesNotExist()));
            http.verify(() -> HttpUtils.execute(any(HttpExecutionRequest.class)));
            http.verify(() -> HttpUtils.close(batch));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"http-error", "malformed-json", "transport-error"})
    void verifyVulnerabilityQueryFailures(final String failure, @TempDir final Path directory) throws Throwable {
        val jar = createDependencyJar(directory, "org.apereo.cas", "cas-server-test", "1.0.0");
        try (val batch = response("http-error".equals(failure) ? 503 : 200, "invalid-json");
             val http = mockStatic(HttpUtils.class)) {
            if ("transport-error".equals(failure)) {
                http.when(() -> HttpUtils.execute(any(HttpExecutionRequest.class)))
                    .thenThrow(new IllegalStateException("OSV unavailable"));
            } else {
                http.when(() -> HttpUtils.execute(any(HttpExecutionRequest.class))).thenReturn(batch);
            }
            withRuntimeClasspath(jar.toString(), () -> {
                val result = mockMvc.perform(get("/actuator/dependencies/vulnerabilities"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dependencyCount").value(1))
                    .andExpect(jsonPath("$.vulnerabilitiesCount").value(0))
                    .andExpect(jsonPath("$.vulnerabilities").doesNotExist())
                    .andExpect(jsonPath("$.errors", hasSize(1)));
                if ("http-error".equals(failure)) {
                    result.andExpect(jsonPath("$.errors[0]").value("OSV querybatch request failed with status code 503 SERVICE_UNAVAILABLE"));
                } else if ("transport-error".equals(failure)) {
                    result.andExpect(jsonPath("$.errors[0]").value("OSV unavailable"));
                }
            });
            http.verify(() -> HttpUtils.execute(any(HttpExecutionRequest.class)));
            if (!"transport-error".equals(failure)) {
                http.verify(() -> HttpUtils.close(batch));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"http-error", "malformed-json"})
    void verifyVulnerabilityDetailFailures(final String failure, @TempDir final Path directory) throws Throwable {
        val jar = createDependencyJar(directory, "org.apereo.cas", "cas-server-test", "1.0.0");
        try (val batch = response(200, """
            {"results":[{"vulns":[{"id":"CVE-TEST-1"},{"id":"CVE-TEST-2"}]}]}
            """);
             val failedDetails = response("http-error".equals(failure) ? 404 : 200, "invalid-json");
             val validDetails = response(200, """
                 {"id":"CVE-TEST-2","summary":"Available finding"}
                 """);
             val http = mockStatic(HttpUtils.class)) {
            http.when(() -> HttpUtils.execute(any(HttpExecutionRequest.class))).thenReturn(batch, failedDetails, validDetails);
            withRuntimeClasspath(jar.toString(), () -> {
                val result = mockMvc.perform(get("/actuator/dependencies/vulnerabilities"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dependencyCount").value(1))
                    .andExpect(jsonPath("$.vulnerabilitiesCount").value(1))
                    .andExpect(jsonPath("$.vulnerabilities", hasSize(1)))
                    .andExpect(jsonPath("$.vulnerabilities[0].details.id").value("CVE-TEST-2"));
                if ("http-error".equals(failure)) {
                    result.andExpect(jsonPath("$.errors", contains(
                        "OSV vulnerability details request failed for CVE-TEST-1 with status code 404 NOT_FOUND")));
                } else {
                    result.andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0]", startsWith("OSV vulnerability details for CVE-TEST-1 cannot be read")));
                }
            });
            http.verify(() -> HttpUtils.execute(any(HttpExecutionRequest.class)), times(3));
            http.verify(() -> HttpUtils.close(batch));
            http.verify(() -> HttpUtils.close(failedDetails));
            http.verify(() -> HttpUtils.close(validDetails));
        }
    }

    @Test
    void verifyNestedDependencies(@TempDir final Path directory) throws Throwable {
        val dependency = createDependencyJar(directory, "org.apereo.cas", "cas-server-test", "1.0.0");
        val application = directory.resolve("application.jar");
        try (val output = new JarOutputStream(Files.newOutputStream(application))) {
            output.putNextEntry(new JarEntry("BOOT-INF/lib/"));
            output.closeEntry();
            output.putNextEntry(new JarEntry("BOOT-INF/lib/cas-server-test-1.0.0.jar"));
            Files.copy(dependency, output);
            output.closeEntry();
        }
        withRuntimeClasspath(application.toString(), () -> mockMvc.perform(get("/actuator/dependencies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].artifactId").value("cas-server-test"))
            .andExpect(jsonPath("$[0].version").value("1.0.0"))
            .andExpect(jsonPath("$[0].source").value("BOOT-INF/lib/cas-server-test-1.0.0.jar")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"groupId", "artifactId", "version"})
    void verifyIncompleteMavenMetadataIsIgnored(final String missing, @TempDir final Path directory) throws Throwable {
        val jar = createDependencyJar(directory, "groupId".equals(missing) ? " " : "org.apereo.cas",
            "artifactId".equals(missing) ? " " : "cas-server-test", "version".equals(missing) ? " " : "1.0.0");
        withRuntimeClasspath(jar.toString(), () -> mockMvc.perform(get("/actuator/dependencies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0))));
    }

    private static BasicClassicHttpResponse response(final int status, final String body) {
        val response = new BasicClassicHttpResponse(status);
        response.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return response;
    }

    private static Path createDependencyJar(final Path directory,
                                            final String groupId,
                                            final String artifactId,
                                            final String version)
            throws IOException {
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
                                             final CheckedRunnable runnable)
            throws Throwable {
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
