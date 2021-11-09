package org.apereo.cas.configuration.model.core.services;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
public class ServiceRegistryCacheProperties implements Serializable {
    private static final long serialVersionUID = -368826011744304210L;

    /**
     * Services cache duration specifies the fixed duration for an
     * entry to be automatically removed from the cache after its creation or update.
     */
    @DurationCapable
    private String duration = "PT15M";

    /**
     * Services cache size specifies the maximum number of entries the cache may contain.
     */
    private long cacheSize = 5_000L;

    /**
     * Services cache capacity sets the minimum total size for the internal data structures.
     */
    private int initialCapacity = 2_000;
}
