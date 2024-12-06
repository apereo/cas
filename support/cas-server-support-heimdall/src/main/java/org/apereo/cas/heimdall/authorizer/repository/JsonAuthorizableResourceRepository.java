package org.apereo.cas.heimdall.authorizer.repository;

import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResources;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.PathWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.hjson.JsonValue;
import org.springframework.util.Assert;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link JsonAuthorizableResourceRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class JsonAuthorizableResourceRepository implements AuthorizableResourceRepository {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final Map<String, List<AuthorizableResource>> resources = new ConcurrentHashMap<>();

    private final File directory;
    private final WatcherService watcherService;

    public JsonAuthorizableResourceRepository(final File directory) {
        this.directory = directory;
        Assert.isTrue(directory.isDirectory(), "JSON directory location must be a valid directory");
        loadJsonResources();
        this.watcherService = new PathWatcherService(directory.toPath(),
            this::loadJsonResourceFrom, this::loadJsonResourceFrom, this::loadJsonResourceFrom);
        this.watcherService.start(getClass().getSimpleName());
    }

    @Override
    public Optional<AuthorizableResource> find(final AuthorizationRequest request) {
        if (resources.containsKey(request.getNamespace())) {
            val authorizableResources = resources.get(request.getNamespace());
            return authorizableResources
                .stream()
                .filter(r -> RegexUtils.find(r.getPattern(), request.getUri()))
                .filter(r -> "*".equalsIgnoreCase(r.getMethod()) || RegexUtils.find(r.getMethod(), request.getMethod()))
                .findFirst();
        }
        return Optional.empty();
    }

    @Override
    public List<AuthorizableResource> find(final String namespace) {
        return List.copyOf(resources.get(namespace));
    }

    @Override
    public Optional<AuthorizableResource> find(final String namespace, final long id) {
        val results = resources.get(namespace);
        return results.stream().filter(r -> r.getId() == id).findFirst();
    }

    @Override
    public Map<String, List<AuthorizableResource>> findAll() {
        return Map.copyOf(resources);
    }

    @Override
    public void destroy() {
        this.watcherService.close();
    }

    private void loadJsonResources() {
        val jsonFiles = FileUtils.listFiles(directory, new String[]{"json"}, true);
        for (val jsonFile : jsonFiles) {
            loadJsonResourceFrom(jsonFile);
        }
    }

    private void loadJsonResourceFrom(final File jsonFile) {
        FunctionUtils.doAndHandle(__ -> {
            try (val reader = new FileReader(jsonFile, StandardCharsets.UTF_8)) {
                val json = JsonValue.readHjson(reader).toString();
                val loadedResource = MAPPER.readValue(json, AuthorizableResources.class);
                resources.put(loadedResource.getNamespace(), loadedResource.getResources());
            }
        });
    }
}
