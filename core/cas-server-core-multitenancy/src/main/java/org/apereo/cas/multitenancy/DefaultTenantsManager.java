package org.apereo.cas.multitenancy;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.WatcherService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hjson.JsonValue;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link DefaultTenantsManager}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class DefaultTenantsManager implements TenantsManager, DisposableBean, InitializingBean {
    private final ObjectMapper objectMapper;

    private final Resource jsonResource;

    private WatcherService watcherService;

    private final List<TenantDefinition> tenantDefinitionList = new ArrayList<>();

    private void initializeWatchService() {
        FunctionUtils.doAndHandle(__ -> {
            if (ResourceUtils.isFile(jsonResource)) {
                watcherService = new FileWatcherService(jsonResource.getFile(),
                    file -> {
                        val resources = readFromJsonResource();
                        if (!resources.isEmpty()) {
                            tenantDefinitionList.clear();
                            tenantDefinitionList.addAll(resources);
                        }
                    });
                watcherService.start(getClass().getSimpleName());
            }
        });
    }

    @Override
    public void destroy() {
        FunctionUtils.doIfNotNull(watcherService, WatcherService::close);
    }

    @Override
    public Optional<TenantDefinition> findTenant(final String tenantId) {
        return tenantDefinitionList
            .stream()
            .filter(t -> t.getId().equalsIgnoreCase(tenantId))
            .findFirst();
    }

    @Override
    public List<TenantDefinition> findTenants() {
        return List.copyOf(tenantDefinitionList);
    }

    private List<TenantDefinition> readFromJsonResource() {
        return FunctionUtils.doAndHandle((CheckedSupplier<List<TenantDefinition>>) () -> {
            if (ResourceUtils.doesResourceExist(jsonResource)) {
                try (val reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
                    val tenantsList = new TypeReference<List<TenantDefinition>>() {
                    };
                    return objectMapper.readValue(JsonValue.readHjson(reader).toString(), tenantsList);
                }
            }
            return new ArrayList<>();
        }, throwable -> new ArrayList<>()).get();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        tenantDefinitionList.addAll(readFromJsonResource());
        initializeWatchService();
    }
}
