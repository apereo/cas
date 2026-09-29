package org.apereo.cas.configuration.model.core.ticket.registry;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link StatelessTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter

@Accessors(chain = true)
public class StatelessTicketRegistryProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2600525447128979994L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public StatelessTicketRegistryProperties() {
        crypto.setEnabled(true);
        crypto.setSigningEnabled(false);
        crypto.getEncryption().setKeySize(EncryptionRandomizedSigningJwtCryptographyProperties.DEFAULT_ENCRYPTION_KEY_SIZE);
    }
}
