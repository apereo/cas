package org.apereo.cas.multitenancy;

import module java.base;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link DefaultTenantsManager}.
 * Tenant definitions are read with the given object mapper, which decodes encrypted property values,
 * and are kept in a map by tenant id that is replaced as a whole on every change, so lookups never see a
 * partial list. Changes are written back from the definitions as stored, so encrypted values and expressions
 * stay as they are in the resource; loading and changes run one at a time, so concurrent changes do not
 * overwrite each other in the resource. Tenant ids must match {@link #TENANT_ID_PATTERN}: definitions with other ids
 * are skipped when loading and refused when saving.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Slf4j
public class DefaultTenantsManager implements TenantsManager, DisposableBean, InitializingBean {
    private static final ObjectMapper STORAGE_MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final ObjectMapper objectMapper;

    private final Resource jsonResource;

    @Nullable
    private WatcherService watcherService;

    private final AtomicReference<Map<String, TenantDefinition>> tenantDefinitions = new AtomicReference<>(new LinkedHashMap<>());

    private final CasReentrantLock lock = new CasReentrantLock();

    private void initializeWatchService() {
        FunctionUtils.doAndHandle(_ -> {
            if (ResourceUtils.isFile(jsonResource)) {
                watcherService = new FileWatcherService(jsonResource.getFile(), _ -> load());
                watcherService.start(getClass().getSimpleName());
            }
        });
    }

    @Override
    public void destroy() {
        FunctionUtils.doIfNotNull(watcherService, WatcherService::close);
    }

    @Override
    public Optional<TenantDefinition> findTenant(@Nullable final String tenantId) {
        return StringUtils.isBlank(tenantId)
            ? Optional.empty()
            : Optional.ofNullable(tenantDefinitions.get().get(toKey(tenantId)));
    }

    @Override
    public List<TenantDefinition> findTenants() {
        return List.copyOf(tenantDefinitions.get().values());
    }

    /**
     * Save a tenant definition. A property whose value is {@link #REDACTED_PROPERTY_VALUE} keeps the value
     * stored for it. When the definitions come from a file, the file is rewritten from the definitions as
     * stored there, with this definition replaced.
     *
     * @param tenantDefinition the tenant definition to register
     * @return the saved definition
     */
    @Override
    public TenantDefinition save(final TenantDefinition tenantDefinition) {
        return lock.execute(() -> saveTenantDefinition(tenantDefinition));
    }

    @Override
    public boolean delete(final String tenantId) {
        return lock.execute(() -> deleteTenantDefinition(tenantId));
    }

    @Override
    public void load() {
        lock.execute(() -> {
            val definitions = FunctionUtils.doAndHandle(
                (CheckedSupplier<List<TenantDefinition>>) () -> readTenantDefinitions(objectMapper),
                throwable -> List.<TenantDefinition>of()).get();
            if (!definitions.isEmpty()) {
                val loaded = new LinkedHashMap<String, TenantDefinition>();
                definitions
                    .stream()
                    .filter(definition -> {
                        val valid = isValidTenantId(definition.getId());
                        if (!valid) {
                            LOGGER.warn("Skipping tenant definition with invalid id [{}]", definition.getId());
                        }
                        return valid;
                    })
                    .forEach(definition -> loaded.put(toKey(definition.getId()), definition));
                tenantDefinitions.set(loaded);
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        load();
        initializeWatchService();
    }

    private TenantDefinition saveTenantDefinition(final TenantDefinition tenantDefinition) {
        if (!isValidTenantId(tenantDefinition.getId())) {
            throw new IllegalArgumentException("Tenant id is invalid: " + tenantDefinition.getId());
        }
        val storedDefinitions = ResourceUtils.isFile(jsonResource)
            ? readTenantDefinitions(STORAGE_MAPPER)
            : new ArrayList<>(findTenants());
        val storedDefinition = storedDefinitions.stream()
            .filter(definition -> isSameTenant(definition, tenantDefinition.getId()))
            .findFirst();
        val definition = withRedactedValuesKept(tenantDefinition, storedDefinition);
        val runtimeDefinition = Optional.ofNullable(objectMapper)
            .map(mapper -> mapper.readValue(STORAGE_MAPPER.writeValueAsString(definition), TenantDefinition.class))
            .orElse(definition);
        tenantDefinitions.updateAndGet(current -> {
            val updated = new LinkedHashMap<>(current);
            updated.put(toKey(runtimeDefinition.getId()), runtimeDefinition);
            return updated;
        });
        if (ResourceUtils.isFile(jsonResource)) {
            storedDefinitions.removeIf(stored -> isSameTenant(stored, definition.getId()));
            storedDefinitions.add(definition);
            writeTenantDefinitions(storedDefinitions);
        }
        return definition;
    }

    private boolean deleteTenantDefinition(final String tenantId) {
        val removed = findTenant(tenantId).isPresent();
        if (removed) {
            val storedDefinitions = ResourceUtils.isFile(jsonResource)
                ? readTenantDefinitions(STORAGE_MAPPER)
                : List.<TenantDefinition>of();
            tenantDefinitions.updateAndGet(current -> {
                val updated = new LinkedHashMap<>(current);
                updated.remove(toKey(tenantId));
                return updated;
            });
            if (ResourceUtils.isFile(jsonResource)) {
                writeTenantDefinitions(storedDefinitions
                    .stream()
                    .filter(stored -> !isSameTenant(stored, tenantId))
                    .toList());
            }
        }
        return removed;
    }

    /**
     * Read the tenant definitions from the resource. A resource that cannot be read or parsed fails rather than
     * reading as empty, so a change never rewrites the resource from a partial list.
     *
     * @param mapper the object mapper that reads the definitions
     * @return the tenant definitions, empty when the resource does not exist
     */
    private List<TenantDefinition> readTenantDefinitions(final ObjectMapper mapper) {
        return FunctionUtils.doUnchecked(() -> {
            if (ResourceUtils.doesResourceExist(jsonResource)) {
                try (val reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
                    val tenantsList = new TypeReference<List<TenantDefinition>>() {
                    };
                    val json = JsonValue.readHjson(reader).toString();
                    return new ArrayList<>(mapper.readValue(json, tenantsList));
                }
            }
            return new ArrayList<TenantDefinition>();
        });
    }

    private void writeTenantDefinitions(final List<TenantDefinition> definitions) {
        FunctionUtils.doAndHandle(_ -> {
            val writer = STORAGE_MAPPER.writerWithDefaultPrettyPrinter();
            writer.writeValue(jsonResource.getFile(), definitions);
        });
    }

    private static TenantDefinition withRedactedValuesKept(final TenantDefinition definition,
                                                           final Optional<TenantDefinition> storedDefinition) {
        val properties = new LinkedHashMap<String, Object>();
        FunctionUtils.doIfNotNull(definition.getProperties(), properties::putAll);
        storedDefinition
            .map(TenantDefinition::getProperties)
            .ifPresent(stored -> properties.replaceAll((name, value) ->
                REDACTED_PROPERTY_VALUE.equals(value) ? stored.getOrDefault(name, value) : value));
        properties.values().removeIf(REDACTED_PROPERTY_VALUE::equals);
        return definition.setProperties(properties);
    }

    private static boolean isValidTenantId(@Nullable final String tenantId) {
        return StringUtils.isNotBlank(tenantId) && TENANT_ID_PATTERN.matcher(tenantId).matches();
    }

    private static boolean isSameTenant(final TenantDefinition definition, final String tenantId) {
        return toKey(definition.getId()).equals(toKey(tenantId));
    }

    private static String toKey(@Nullable final String tenantId) {
        return StringUtils.defaultString(tenantId).toLowerCase(Locale.ENGLISH);
    }
}
