package org.apereo.cas.configuration.model.core.util;

import org.apereo.cas.configuration.model.core.ticket.ProxyGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ProxyTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ServiceTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.TicketGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.TransientSessionTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.registry.TicketRegistryProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * Configuration properties class for {@code ticket}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class TicketProperties implements Serializable {

    private static final long serialVersionUID = 5586947805593202037L;

    /**
     * Properties and settings related to session-transient tickets.
     */
    @NestedConfigurationProperty
    private TransientSessionTicketProperties tst = new TransientSessionTicketProperties();

    /**
     * Properties and settings related to proxy-granting tickets.
     */
    @NestedConfigurationProperty
    private ProxyGrantingTicketProperties pgt = new ProxyGrantingTicketProperties();

    /**
     * Properties and settings related to ticket encryption.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    /**
     * Properties and settings related to proxy tickets.
     */
    @NestedConfigurationProperty
    private ProxyTicketProperties pt = new ProxyTicketProperties();

    /**
     * Properties and settings related to ticket registry.
     */
    @NestedConfigurationProperty
    private TicketRegistryProperties registry = new TicketRegistryProperties();

    /**
     * Properties and settings related to service tickets.
     */
    @NestedConfigurationProperty
    private ServiceTicketProperties st = new ServiceTicketProperties();

    /**
     * Properties and settings related to ticket-granting tickets.
     */
    @NestedConfigurationProperty
    private TicketGrantingTicketProperties tgt = new TicketGrantingTicketProperties();

    public TicketProperties() {
        crypto.setEnabled(false);
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
