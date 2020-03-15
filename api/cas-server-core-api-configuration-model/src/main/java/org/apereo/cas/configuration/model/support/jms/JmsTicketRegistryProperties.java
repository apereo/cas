package org.apereo.cas.configuration.model.support.jms;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link JmsTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-jms-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class JmsTicketRegistryProperties implements Serializable {

    private static final long serialVersionUID = -2600525447128979994L;

    /**
     * Identifier for this CAS server node
     * that tags the sender/receiver in the JMS queue
     * and avoid processing of inbound calls.
     * If left blank, an identifier is generated automatically
     * and kept in memory.
     */
    private String queueIdentifier;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();
}
