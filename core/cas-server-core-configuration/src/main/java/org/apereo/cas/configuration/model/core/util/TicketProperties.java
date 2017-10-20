package org.apereo.cas.configuration.model.core.util;

import org.apereo.cas.configuration.model.core.ticket.ProxyGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ProxyTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ServiceTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.TicketGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.registry.TicketRegistryProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * Configuration properties class for {@code ticket}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
public class TicketProperties implements Serializable {

    private static final long serialVersionUID = 5586947805593202037L;
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
        this.crypto.setEnabled(false);
    }

    public ProxyGrantingTicketProperties getPgt() {
        return pgt;
    }

    public void setPgt(final ProxyGrantingTicketProperties pgt) {
        this.pgt = pgt;
    }

    public ProxyTicketProperties getPt() {
        return pt;
    }

    public void setPt(final ProxyTicketProperties pt) {
        this.pt = pt;
    }

    public TicketRegistryProperties getRegistry() {
        return registry;
    }

    public void setRegistry(final TicketRegistryProperties registry) {
        this.registry = registry;
    }

    public ServiceTicketProperties getSt() {
        return st;
    }

    public void setSt(final ServiceTicketProperties st) {
        this.st = st;
    }

    public TicketGrantingTicketProperties getTgt() {
        return tgt;
    }

    public void setTgt(final TicketGrantingTicketProperties tgt) {
        this.tgt = tgt;
    }

    public EncryptionJwtSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionJwtSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }
}
