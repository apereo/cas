package org.apereo.cas.configuration.model.support.multitenancy;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link MultitenancyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-core-multitenancy")
@Getter
@Setter
@Accessors(chain = true)
public class MultitenancyProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 6599875416590735492L;

    /**
     * Load tenant definitions from a JSON resource.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties json = new SpringResourceProperties();

    /**
     * Core settings for multitenancy.
     */
    @NestedConfigurationProperty
    private MultitenancyCoreProperties core = new MultitenancyCoreProperties();

}
