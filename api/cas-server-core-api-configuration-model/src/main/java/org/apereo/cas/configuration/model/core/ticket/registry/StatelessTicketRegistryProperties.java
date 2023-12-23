package org.apereo.cas.configuration.model.core.ticket.registry;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link StatelessTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter
@JsonFilter("StatelessTicketRegistryProperties")
@Accessors(chain = true)
public class StatelessTicketRegistryProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2600525447128979994L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties().setEnabled(true);

    /**
     * Control whether the registry is enabled.
     */
    private boolean enabled;
}
