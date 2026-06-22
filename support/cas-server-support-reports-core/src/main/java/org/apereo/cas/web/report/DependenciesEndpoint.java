package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import tools.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

/**
 * This is {@link DependenciesEndpoint}.
 * This endpoint scans the runtime classpath for JAR files, inspects their Maven metadata,
 * and compiles a report of dependencies.
 * The report includes the groupId, artifactId, version, and source of each dependency.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Endpoint(id = "dependencies", defaultAccess = Access.NONE)
@Slf4j
public class DependenciesEndpoint extends BaseCasRestActuatorEndpoint {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false)
        .build()
        .toObjectMapper();

    private static final String OSV_QUERY_BATCH_URL = "https://api.osv.dev/v1/querybatch";

    private static final int OSV_MAX_BATCH_SIZE = 1000;

    public DependenciesEndpoint(final ConfigurableApplicationContext applicationContext,
                                final CasConfigurationProperties casProperties) {
        super(casProperties, applicationContext);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    @Operation(summary = "Provide a report of application libraries")
    public Set<Dependency> getDependencies() {
        return scanRuntimeDependencies();
    }

    /**
     * Gets vulnerabilities.
     *
     * @return the vulnerabilities
     */
    @GetMapping(
        path = "/vulnerabilities",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.ALL_VALUE)
    @Operation(summary = "Provide a report of application libraries and their reported vulnerabilities")
    public VulnerabilityReport getVulnerabilities() {
        val dependencies = scanRuntimeDependencies();
        val vulnerabilities = new ArrayList<DependencyVulnerability>();
        val errors = new ArrayList<String>();

        val dependencyList = new ArrayList<>(dependencies);
        for (var fromIndex = 0; fromIndex < dependencyList.size(); fromIndex += OSV_MAX_BATCH_SIZE) {
            val toIndex = Math.min(fromIndex + OSV_MAX_BATCH_SIZE, dependencyList.size());
            val batch = dependencyList.subList(fromIndex, toIndex);
            queryOsvBatch(batch, vulnerabilities, errors);
        }

        val vulnerableDependencies = vulnerabilities
            .stream()
            .map(DependencyVulnerability::dependency)
            .distinct()
            .count();

        return new VulnerabilityReport(dependencies.size(),
            vulnerableDependencies, vulnerabilities, errors);
    }

    private static void queryOsvBatch(final List<Dependency> dependencies,
                                      final Collection<DependencyVulnerability> vulnerabilities,
                                      final Collection<String> errors) {
        var pageToken = StringUtils.EMPTY;
        do {
            try {
                val requestBody = buildOsvBatchRequest(dependencies, pageToken);
                val entity = MAPPER.writeValueAsString(requestBody);
                val exec = HttpExecutionRequest.builder()
                    .url(OSV_QUERY_BATCH_URL)
                    .entity(entity)
                    .method(HttpMethod.POST)
                    .build();

                val response = HttpUtils.execute(exec);
                try {
                    val statusCode = HttpStatus.valueOf(response.getCode());
                    if (!statusCode.is2xxSuccessful()) {
                        errors.add("OSV querybatch request failed with status code " + statusCode);
                        return;
                    }

                    try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val responseBody = IOUtils.toString(content, StandardCharsets.UTF_8);
                        val osvResponse = MAPPER.readValue(responseBody, BatchResponse.class);
                        mapOsvResults(dependencies, osvResponse, vulnerabilities, errors);
                        pageToken = StringUtils.defaultString(osvResponse.nextPageToken());
                    }
                } finally {
                    HttpUtils.close(response);
                }
            } catch (final Exception e) {
                LOGGER.warn("Unable to query OSV for runtime dependency vulnerabilities", e);
                errors.add(e.getMessage());
                return;
            }
        } while (StringUtils.isNotBlank(pageToken));
    }

    private static Map<String, Object> buildOsvBatchRequest(final List<Dependency> dependencies,
                                                            final String pageToken) {
        val queries = dependencies
            .stream()
            .map(dependency -> Map.of(
                "version", dependency.version(),
                "package", Map.of("name", dependency.name(), "ecosystem", "Maven")))
            .toList();

        val request = new LinkedHashMap<String, Object>();
        request.put("queries", queries);
        if (StringUtils.isNotBlank(pageToken)) {
            request.put("page_token", pageToken);
        }
        return request;
    }

    private static void mapOsvResults(final List<Dependency> dependencies,
                                      final BatchResponse response,
                                      final Collection<DependencyVulnerability> vulnerabilities,
                                      final Collection<String> errors) {
        val osvResults = response.results() == null ? List.<QueryResult>of() : response.results();
        for (var i = 0; i < dependencies.size(); i++) {
            val dependency = dependencies.get(i);
            val queryResult = i < osvResults.size() ? osvResults.get(i) : new QueryResult(List.of());
            val osvVulnerabilities = queryResult.vulnerabilities() == null
                ? List.<Vulnerability>of()
                : queryResult.vulnerabilities();
            osvVulnerabilities
                .stream()
                .map(vulnerability -> getOsvVulnerabilityDetails(dependency, vulnerability, errors))
                .filter(Objects::nonNull)
                .forEach(vulnerabilities::add);
        }
    }

    private static @Nullable DependencyVulnerability getOsvVulnerabilityDetails(
        final Dependency dependency, final Vulnerability vulnerability, final Collection<String> errors) {
        val url = "https://api.osv.dev/v1/vulns/" + vulnerability.id();
        val exec = HttpExecutionRequest.builder()
            .url(url)
            .method(HttpMethod.GET)
            .build();

        val response = HttpUtils.execute(exec);
        try {
            val statusCode = HttpStatus.valueOf(response.getCode());
            if (statusCode.is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val responseBody = IOUtils.toString(content, StandardCharsets.UTF_8);
                    val details = MAPPER.readValue(responseBody, VulnerabilityDetails.class);
                    return new DependencyVulnerability(dependency, details);
                }
            } else {
                errors.add("OSV vulnerability details request failed for " + vulnerability.id() + " with status code " + statusCode);
            }
        } catch (final Exception e) {
            LOGGER.warn("Unable to query OSV vulnerability details for [{}]", vulnerability.id(), e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    protected Set<Dependency> scanRuntimeDependencies() {
        val dependencies = new LinkedHashSet<Dependency>();
        scanClasspathEntries(dependencies);
        scanClassLoaderUrls(dependencies);
        return dependencies;
    }

    private static void scanClasspathEntries(final Set<Dependency> dependencies) {
        val classpath = System.getProperty("java.class.path", StringUtils.EMPTY);
        if (StringUtils.isBlank(classpath)) {
            return;
        }

        for (val entry : Splitter.on(Pattern.compile(File.pathSeparator)).split(classpath)) {
            if (StringUtils.isBlank(entry)) {
                continue;
            }
            scanJarPath(Paths.get(entry), dependencies);
        }
    }

    private static void scanClassLoaderUrls(final Set<Dependency> dependencies) {
        val classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader instanceof URLClassLoader urlClassLoader) {
            for (val url : urlClassLoader.getURLs()) {
                if ("file".equalsIgnoreCase(url.getProtocol())) {
                    scanJarPath(Paths.get(url.getPath()), dependencies);
                }
            }
        }
    }

    private static void scanJarPath(final Path path, final Set<Dependency> dependencies) {
        if (!Files.isRegularFile(path) || !path.toString().endsWith(".jar")) {
            return;
        }

        try (val jarFile = new JarFile(path.toFile())) {
            scanJarEntries(jarFile, path.getFileName().toString(), dependencies);
            scanNestedSpringBootLibraries(jarFile, dependencies);
        } catch (final Exception e) {
            LOGGER.trace("Unable to inspect runtime JAR [{}] for Maven metadata", path, e);
        }
    }

    private static void scanJarEntries(final JarFile jarFile,
                                       final String source,
                                       final Set<Dependency> dependencies) throws IOException {
        val entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement();
            if (isMavenPomProperties(entry.getName())) {
                try (val inputStream = jarFile.getInputStream(entry)) {
                    readMavenDependency(inputStream, source).ifPresent(dependencies::add);
                }
            }
        }
    }

    private static void scanNestedSpringBootLibraries(final JarFile jarFile,
                                                      final Set<Dependency> dependencies) throws IOException {
        val entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement();
            if (!entry.isDirectory()
                && entry.getName().startsWith("BOOT-INF/lib/")
                && entry.getName().endsWith(".jar")) {
                try (val inputStream = jarFile.getInputStream(entry)) {
                    scanNestedJar(inputStream, entry.getName(), dependencies);
                }
            }
        }
    }

    private static void scanNestedJar(final InputStream inputStream,
                                      final String source,
                                      final Set<Dependency> dependencies) throws IOException {
        try (val jarInputStream = new JarInputStream(inputStream)) {
            for (var entry = jarInputStream.getNextJarEntry();
                 entry != null;
                 entry = jarInputStream.getNextJarEntry()) {

                if (isMavenPomProperties(entry.getName())) {
                    val buffer = new ByteArrayOutputStream();
                    jarInputStream.transferTo(buffer);

                    try (val dependencyInputStream = new ByteArrayInputStream(buffer.toByteArray())) {
                        readMavenDependency(dependencyInputStream, source)
                            .ifPresent(dependencies::add);
                    }
                }
            }
        }
    }

    private static boolean isMavenPomProperties(final String entryName) {
        return entryName.startsWith("META-INF/maven/") && entryName.endsWith("/pom.properties");
    }

    private static Optional<Dependency> readMavenDependency(final InputStream inputStream,
                                                            final String source) throws IOException {
        val properties = new Properties();
        properties.load(inputStream);

        val groupId = StringUtils.trimToNull(properties.getProperty("groupId"));
        val artifactId = StringUtils.trimToNull(properties.getProperty("artifactId"));
        val version = StringUtils.trimToNull(properties.getProperty("version"));

        if (groupId == null || artifactId == null || version == null) {
            return Optional.empty();
        }
        return Optional.of(new Dependency(groupId, artifactId, version, source));
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record Dependency(String groupId, String artifactId, String version, String source) {
        /**
         * Name. 
         *
         * @return the string
         */
        public String name() {
            return groupId + ':' + artifactId;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record VulnerabilityReport(long dependencyCount, long vulnerabilitiesCount,
        Collection<DependencyVulnerability> vulnerabilities, Collection<String> errors) {
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record DependencyVulnerability(Dependency dependency, VulnerabilityDetails details) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private record BatchResponse(List<QueryResult> results,
        @JsonProperty("next_page_token") String nextPageToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private record QueryResult(@JsonProperty("vulns") List<Vulnerability> vulnerabilities) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Vulnerability(String id, String modified) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record VulnerabilityDetails(
        String id,
        String summary,
        String details,
        Collection<String> aliases,
        String published,
        String modified,
        Collection<Severity> severity,
        Collection<Affected> affected,
        Collection<Reference> references) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Severity(String type, String score) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record Affected(
        PackageDetails pkg,
        @JsonProperty("ecosystem_specific")
        Map<String, Object> ecosystem,
        @JsonProperty("database_specific")
        Map<String, Object> database
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record PackageDetails(
        String ecosystem,
        String name,
        String purl
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record Reference(String type, String url) {
    }
}
