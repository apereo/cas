package org.apereo.cas.configuration.model.core.ticket.registry;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link InMemoryTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter

public class InMemoryTicketRegistryProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -2600525447128979994L;
    
    /**
     * The initial capacity of the underlying memory store.
     * The implementation performs internal sizing to accommodate this many elements.
     */
    private int initialCapacity = 1000;

    /**
     * The load factor threshold, used to control resizing.
     * Resizing may be performed when the average number of elements per bin exceeds this threshold.
     */
    private int loadFactor = 1;

    /**
     * The estimated number of concurrently updating threads.
     * The implementation performs internal sizing to try to accommodate this many threads.
     */
    private int concurrency = 20;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public InMemoryTicketRegistryProperties() {
        crypto.setEnabled(false);
    }

}
