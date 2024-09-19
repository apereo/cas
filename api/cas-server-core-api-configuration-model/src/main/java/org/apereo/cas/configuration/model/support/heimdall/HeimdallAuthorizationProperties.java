package org.apereo.cas.configuration.model.support.heimdall;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link HeimdallAuthorizationProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-heimdall")
@Getter
@Setter
@Accessors(chain = true)
public class HeimdallAuthorizationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 3634916460241033347L;

    /**
     * JSON directory resource that contains all authorizable resources
     * as flat JSON files.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties json = new SpringResourceProperties();
}
