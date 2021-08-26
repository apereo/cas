package org.apereo.cas.entity;

import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

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
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

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
                        LoggingUtils.error(LOGGER, e);
                    }
                });
                discoveryFeedResourceWatchers.start(getClass().getSimpleName());
            }
        }
    }

    public SamlIdentityProviderEntityParser(final SamlIdentityProviderEntity... entity) {
        identityProviders.addAll(Arrays.asList(entity));
    }

    /**
     * Clear providers.
     */
    public void clear() {
        identityProviders.clear();
    }

    /**
     * Import resource and provide boolean.
     *
     * @param resource the resource
     * @return the boolean
     */
    public boolean importResource(final Resource resource) {
        try {
            if (ResourceUtils.doesResourceExist(resource)) {
                try (val reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    val ref = new TypeReference<List<SamlIdentityProviderEntity>>() {
                    };
                    identityProviders.addAll(MAPPER.readValue(JsonValue.readHjson(reader).toString(), ref));
                }
                return true;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
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
