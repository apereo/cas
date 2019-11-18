package org.apereo.cas.entity;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link SamlIdentityProviderEntityParser}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class SamlIdentityProviderEntityParser implements DisposableBean {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Getter
    private final Set<SamlIdentityProviderEntity> identityProviders = new LinkedHashSet<>(0);

    private FileWatcherService discoveryFeedResourceWatchers;

    public SamlIdentityProviderEntityParser(final Resource resource) throws Exception {
        if (importResource(resource)) {
            if (ResourceUtils.isFile(resource)) {
                discoveryFeedResourceWatchers = new FileWatcherService(resource.getFile(), file -> {
                    try {
                        LOGGER.trace("Reloading identity providers...");
                        clear();
                        importResource(resource);
                    } catch (final Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
                discoveryFeedResourceWatchers.start(getClass().getSimpleName());
            }
        }
    }

    public SamlIdentityProviderEntityParser(final SamlIdentityProviderEntity... entity) {
        identityProviders.addAll(Arrays.asList(entity));
    }

    public void clear() {
        identityProviders.clear();
    }

    public boolean importResource(final Resource resource) {
        try {
            if (ResourceUtils.doesResourceExist(resource)) {
                try (val reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    final TypeReference<List<SamlIdentityProviderEntity>> ref = new TypeReference<>() {
                    };
                    identityProviders.addAll(MAPPER.readValue(JsonValue.readHjson(reader).toString(), ref));
                }
                return true;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void destroy() {
        if (discoveryFeedResourceWatchers != null) {
            discoveryFeedResourceWatchers.close();
        }
    }
}
