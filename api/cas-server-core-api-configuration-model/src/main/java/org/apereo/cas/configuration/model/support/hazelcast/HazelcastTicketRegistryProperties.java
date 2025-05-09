package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;

/**
 * Encapsulates hazelcast properties exposed by CAS via properties file property source in a type-safe manner.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class HazelcastTicketRegistryProperties extends BaseHazelcastProperties {

    @Serial
    private static final long serialVersionUID = -1095208036374406772L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public HazelcastTicketRegistryProperties() {
        this.crypto.setEnabled(false);
    }
}
