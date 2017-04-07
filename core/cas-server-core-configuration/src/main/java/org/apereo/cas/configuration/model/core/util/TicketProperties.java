package org.apereo.cas.configuration.model.core.util;

import org.apereo.cas.configuration.model.core.ticket.ProxyGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ProxyTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ServiceTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.TicketGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.SigningEncryptionProperties;
import org.apereo.cas.configuration.model.core.ticket.registry.TicketRegistryProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for {@code ticket}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class TicketProperties {

    @NestedConfigurationProperty
    private ProxyGrantingTicketProperties pgt = new ProxyGrantingTicketProperties();

    @NestedConfigurationProperty
    private SigningEncryptionProperties security = new SigningEncryptionProperties();
    
    @NestedConfigurationProperty
    private ProxyTicketProperties pt = new ProxyTicketProperties();

    @NestedConfigurationProperty
    private TicketRegistryProperties registry = new TicketRegistryProperties();

    @NestedConfigurationProperty
    private ServiceTicketProperties st = new ServiceTicketProperties();

    @NestedConfigurationProperty
    private TicketGrantingTicketProperties tgt = new TicketGrantingTicketProperties();
    
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

    public SigningEncryptionProperties getSecurity() {
        return security;
    }

    public void setSecurity(final SigningEncryptionProperties security) {
        this.security = security;
    }
}
