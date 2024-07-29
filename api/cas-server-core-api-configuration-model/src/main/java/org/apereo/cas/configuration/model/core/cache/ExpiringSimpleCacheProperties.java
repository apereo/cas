package org.apereo.cas.configuration.model.core.cache;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link ExpiringSimpleCacheProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-util", automated = true)
@Getter
@Setter
@Accessors(chain = true)

public class ExpiringSimpleCacheProperties extends SimpleCacheProperties {
    @Serial
    private static final long serialVersionUID = -268826011744304210L;

    /**
     * Cache duration specifies the fixed duration for an
     * entry to be automatically removed from the cache after its creation.
     */
    @DurationCapable
    private String duration = "PT15M";
}
