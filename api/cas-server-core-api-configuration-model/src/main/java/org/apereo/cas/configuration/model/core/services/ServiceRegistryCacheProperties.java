package org.apereo.cas.configuration.model.core.services;

import org.apereo.cas.configuration.model.core.cache.SimpleCacheProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link ServiceRegistryCacheProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-services", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ServiceRegistryCacheProperties")
public class ServiceRegistryCacheProperties extends SimpleCacheProperties {
    @Serial
    private static final long serialVersionUID = -368826011744304210L;

    /**
     * Services cache duration specifies the fixed duration for an
     * entry to be automatically removed from the cache after its creation or update.
     */
    @DurationCapable
    private String duration = "PT15M";
}
