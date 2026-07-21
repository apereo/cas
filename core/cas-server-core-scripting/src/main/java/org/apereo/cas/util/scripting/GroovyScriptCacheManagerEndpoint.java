package org.apereo.cas.util.scripting;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.Couplet;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * This is {@link GroovyScriptCacheManagerEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@Endpoint(id = "groovyCache", defaultAccess = Access.NONE)
public class GroovyScriptCacheManagerEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<ScriptResourceCacheManager<String, ExecutableCompiledScript>> cacheManagerProvider;

    public GroovyScriptCacheManagerEndpoint(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<ScriptResourceCacheManager<String, ExecutableCompiledScript>> cacheManagerProvider) {
        super(casProperties, applicationContext);
        this.cacheManagerProvider = cacheManagerProvider;
    }

    /**
     * Fetch all keys.
     *
     * @return the collection
     */
    @GetMapping(path = "/keys", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all script cache keys registered and loaded with CAS")
    public List<Couplet> allKeys() {
        val cacheManager = cacheManagerProvider.getObject();
        val keys = cacheManager.getKeys();
        return keys
            .stream()
            .map(key -> {
                val executable = Objects.requireNonNull(cacheManager.get(key),
                    () -> "Script cache entry not found for key [%s]".formatted(key));
                val resource = executable.getResource();
                val resourceName = FunctionUtils.doUnchecked(() ->
                    ResourceUtils.isFile(resource) ? resource.getURI().toASCIIString() : resource.getDescription());
                return new Couplet(key, resourceName);
            })
            .toList();
    }

    /**
     * Fetch script for key.
     *
     * @param key the key
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE, path = "/resources/{key}")
    @Operation(summary = "Get a script cache entry by its key",
        parameters = @Parameter(name = "key", in = ParameterIn.PATH,
            description = "The script cache key to fetch the script for"))
    public ResponseEntity<Resource> fetchScript(@PathVariable final String key) throws Exception {
        val executable = Objects.requireNonNull(cacheManagerProvider.getObject().get(key),
            () -> "Script cache entry not found for key [%s]".formatted(key));
        val resource = executable.getResource();
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .contentLength(resource.contentLength())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(resource.getFilename()))
            .body(resource);
    }

    /**
     * Remove cached key.
     *
     * @param key the key
     */
    @DeleteMapping(path = "/keys/{key}")
    @Operation(summary = "Remove a script cache entry by its key",
        parameters = @Parameter(name = "key", in = ParameterIn.PATH,
            description = "The script cache key to remove the script for"))
    public void removeKey(@PathVariable final String key) {
        cacheManagerProvider.getObject().remove(key);
    }

    /**
     * Recompute.
     *
     * @param key the key
     * @throws Exception the exception
     */
    @Operation(summary = "Recompute a script cache entry by its key",
        parameters = @Parameter(name = "key", in = ParameterIn.PATH,
            description = "The script cache key to use when recomputing the script"))
    @PostMapping(path = "/keys/{key}")
    public void recompute(@PathVariable final String key) throws Exception {
        val cacheManager = cacheManagerProvider.getObject();
        val executable = Objects.requireNonNull(cacheManager.get(key),
            () -> "Script cache entry not found for key [%s]".formatted(key));
        val resource = executable.getResource();
        cacheManager.remove(key);
        if (ResourceUtils.isFile(resource)) {
            cacheManager.cacheScriptableResource(resource.getURI().toASCIIString(), key);
        } else {
            try (val is = resource.getInputStream()) {
                val script = IOUtils.toString(is, StandardCharsets.UTF_8);
                cacheManager.cacheScriptableResource(script, key);
            }
        }
    }

    /**
     * Validate script.
     *
     * @param script the script
     * @return the response entity
     * @throws Exception the exception
     */
    @Operation(summary = "Parse and compile the given inline script")
    @PostMapping(path = "/resources/validate", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity validate(@RequestBody final String script) throws Exception {
        return FunctionUtils.doAndHandle(() -> {
            var resourceToUse = script;
            if (ScriptingUtils.isInlineGroovyScript(resourceToUse)) {
                val matcher = ScriptingUtils.getMatcherForInlineGroovyScript(resourceToUse);
                if (matcher.find()) {
                    resourceToUse = matcher.group(1);
                }
            }
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            return Optional.ofNullable(scriptFactory.fromScript(resourceToUse).compileScript())
                .map(result -> ResponseEntity.status(HttpStatus.OK).build())
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }, e -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CollectionUtils.wrap("error", e.getMessage()))).get();
    }
}
