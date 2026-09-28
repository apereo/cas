package org.apereo.cas.heimdall.authorizer.repository;

import module java.base;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResources;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.io.PathWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.springframework.util.Assert;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link JsonAuthorizableResourceRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class JsonAuthorizableResourceRepository implements AuthorizableResourceRepository {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private volatile ResourceIndex index = new ResourceIndex(Map.of(), Map.of());
    private final Map<Path, AuthorizableResources> documents = new LinkedHashMap<>();
    private final Map<Path, WatcherService> watchers = new LinkedHashMap<>();
    private final CasReentrantLock lock = new CasReentrantLock();
    private final File directory;

    public JsonAuthorizableResourceRepository(final File directory) {
        this.directory = directory.getAbsoluteFile();
        Assert.isTrue(directory.isDirectory(), "JSON directory location must be a valid directory");
        lock.executeAndThrow(() -> {
            loadDirectory(this.directory.toPath());
            return null;
        });
    }

    @Override
    public Optional<AuthorizableResource> find(final AuthorizationRequest request) {
        val authorizableResources = find(request.getNamespace());
        if (!authorizableResources.isEmpty()) {
            return authorizableResources
                .stream()
                .filter(r -> r.getPattern() != null && RegexUtils.find(r.getPattern(), request.getUri()))
                .filter(r -> "*".equalsIgnoreCase(r.getMethod()) || RegexUtils.find(r.getMethod(), request.getMethod()))
                .findFirst();
        }
        return Optional.empty();
    }

    @Override
    public List<AuthorizableResource> find(final String namespace) {
        return namespace == null ? List.of() : index.byNamespace().getOrDefault(namespace, List.of());
    }

    @Override
    public List<AuthorizableResource> find(final AuthZenResource resource, final AuthZenAction action) {
        if (resource == null || resource.getType() == null) {
            return List.of();
        }
        return index.byResourceType()
            .getOrDefault(resource.getType(), List.of())
            .stream()
            .filter(entry -> entry.supports(resource, action))
            .toList();
    }

    @Override
    public Optional<AuthorizableResource> find(final String namespace, final long id) {
        val results = find(namespace);
        return results.stream().filter(r -> r.getId() == id).findFirst();
    }

    @Override
    public AuthorizableResources store(final AuthorizableResources resource) {
        return lock.executeAndThrow(() -> createAuthorizableResources(resource));
    }

    @Override
    public Map<String, List<AuthorizableResource>> findAll() {
        return index.byNamespace();
    }

    @Override
    public void destroy() {
        lock.execute(() -> {
            watchers.values().forEach(WatcherService::close);
            watchers.clear();
        });
    }

    /**
     * Watch every directory that contributes policies, including directories created after startup.
     * The caller holds the repository lock while installing watchers and publishing snapshots.
     *
     * @param path the directory
     * @throws IOException if the directory cannot be listed
     */
    private void loadDirectory(final Path path) throws IOException {
        if (!watchers.containsKey(path)) {
            val watcher = new PathWatcherService(path, this::reload, this::reload, this::reload);
            watchers.put(path, watcher);
            watcher.start(getClass().getSimpleName());
        }
        try (val entries = Files.list(path)) {
            for (val entry : entries.toList()) {
                if (Files.isDirectory(entry) && !Files.isSymbolicLink(entry)) {
                    loadDirectory(entry);
                } else if (entry.toString().endsWith(".json")) {
                    loadJsonResourceFrom(entry);
                }
            }
        }
    }

    /**
     * Reconcile the current file state, so delayed delete events cannot remove a replacement file.
     * Every event also drops documents whose files are gone: a polling watch service (macOS) never
     * reports a file that was created and deleted between two polls.
     *
     * @param file the changed file or directory
     */
    private void reload(final File file) {
        lock.executeAndThrow(() -> {
            val path = file.toPath().toAbsolutePath().normalize();
            if (Files.isDirectory(path) && !Files.isSymbolicLink(path)) {
                loadDirectory(path);
            } else if (!Files.exists(path)) {
                watchers.entrySet().removeIf(entry -> {
                    if (entry.getKey().startsWith(path)) {
                        entry.getValue().close();
                        return true;
                    }
                    return false;
                });
            } else if (path.toString().endsWith(".json")) {
                loadJsonResourceFrom(path);
            }
            documents.keySet().removeIf(key -> !Files.exists(key));
            publishResources();
            return null;
        });
    }

    private void loadJsonResourceFrom(final Path path) {
        try (val reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            val loadedResource = MAPPER.readValue(JsonValue.readHjson(reader).toString(), AuthorizableResources.class);
            Assert.hasText(loadedResource.getNamespace(), "Policy namespace is required");
            documents.put(path, loadedResource);
        } catch (final Exception e) {
            documents.remove(path);
            LoggingUtils.error(LOGGER, e);
        }
        publishResources();
    }

    /**
     * Atomically replace the namespace and AuthZEN resource type indexes; conflicting namespace owners fail closed.
     */
    private void publishResources() {
        val byNamespace = new HashMap<String, List<AuthorizableResource>>();
        documents.values().forEach(document -> byNamespace.merge(document.getNamespace(),
            List.copyOf(document.getResources()), (first, second) -> List.of()));
        val byResourceType = new HashMap<String, List<AuthorizableResource>>();
        byNamespace.values()
            .stream()
            .flatMap(List::stream)
            .filter(resource -> resource.getResourceType() != null)
            .forEach(resource -> byResourceType.computeIfAbsent(resource.getResourceType(), type -> new ArrayList<>()).add(resource));
        byResourceType.replaceAll((type, entries) -> List.copyOf(entries));
        index = new ResourceIndex(Map.copyOf(byNamespace), Map.copyOf(byResourceType));
    }

    private record ResourceIndex(Map<String, List<AuthorizableResource>> byNamespace,
                                 Map<String, List<AuthorizableResource>> byResourceType) {
    }

    private AuthorizableResources createAuthorizableResources(final AuthorizableResources resources) throws IOException {
        Assert.hasText(resources.getNamespace(), "Policy namespace is required");
        val root = directory.toPath().toAbsolutePath().normalize();
        val target = root.resolve(resources.getNamespace() + ".json").normalize();
        Assert.isTrue(root.equals(target.getParent()), "Policy namespace must be a file name");
        val temporary = Files.createTempFile(root, "heimdall-", ".tmp");
        try {
            Files.writeString(temporary, MAPPER.writeValueAsString(resources), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (final AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            loadJsonResourceFrom(target);
            return resources;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
