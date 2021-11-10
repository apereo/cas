package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OidcJsonWebKeyStoreRotationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OidcJsonWebKeyStoreRotationProperties")
public class OidcJsonWebKeyStoreRotationProperties implements Serializable {
    private static final long serialVersionUID = 4988981831781991617L;

    /**
     * Scheduler settings to indicate how often keys are rotated.
     */
    @NestedConfigurationProperty
    private SchedulingProperties schedule = new SchedulingProperties().setRepeatInterval("P90D");
}
