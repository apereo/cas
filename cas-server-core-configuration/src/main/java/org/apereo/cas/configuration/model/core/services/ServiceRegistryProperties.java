package org.apereo.cas.configuration.model.core.services;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for service.registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "service.registry.", ignoreUnknownFields = false)
public class ServiceRegistryProperties {

    private boolean initFromJson = true;

    public boolean isInitFromJson() {
        return initFromJson;
    }

    public void setInitFromJson(final boolean initFromJson) {
        this.initFromJson = initFromJson;
    }
}
