package org.apereo.cas.configuration.model.core;

import org.apereo.cas.configuration.model.core.cache.ExpiringSimpleCacheProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link CasServerCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class CasServerCoreProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 8876382696803430817L;

    /**
     * Settings that control the internal cache engine
     * used to load, parse and hold precompiled groovy scripts.
     */
    @NestedConfigurationProperty
    private ExpiringSimpleCacheProperties groovyCacheManager = new ExpiringSimpleCacheProperties().setDuration("PT8H");
}
