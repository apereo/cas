package org.apereo.cas.configuration.model.core.util;

import org.apereo.cas.configuration.model.core.ticket.ProxyGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ProxyTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ServiceTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.TicketGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.registry.TicketRegistryProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for <code>ticket</code>.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class TicketProperties extends AbstractCryptographyProperties {

    @NestedConfigurationProperty
    private ProxyGrantingTicketProperties pgt = new ProxyGrantingTicketProperties();

    @NestedConfigurationProperty
    private ProxyTicketProperties pt = new ProxyTicketProperties();

    @NestedConfigurationProperty
    private TicketRegistryProperties ticketRegistry = new TicketRegistryProperties();

    @NestedConfigurationProperty
    private ServiceTicketProperties st = new ServiceTicketProperties();

    @NestedConfigurationProperty
    private TicketGrantingTicketProperties tgt = new TicketGrantingTicketProperties();
    
    private Registry registry = new Registry();

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(final Registry registry) {
        this.registry = registry;
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

    public TicketRegistryProperties getTicketRegistry() {
        return ticketRegistry;
    }

    public void setTicketRegistry(final TicketRegistryProperties ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
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

    public static class Registry {
        private Cleaner cleaner = new Cleaner();
        
        public Registry() {
        }

        public Cleaner getCleaner() {
            return cleaner;
        }

        public void setCleaner(final Cleaner cleaner) {
            this.cleaner = cleaner;
        }

        public static class Cleaner {
            private boolean enabled;
            private int startDelay;
            private int repeatInterval;
            
            public Cleaner() {
            }


            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(final boolean enabled) {
                this.enabled = enabled;
            }

            public int getStartDelay() {
                return startDelay;
            }

            public void setStartDelay(final int startDelay) {
                this.startDelay = startDelay;
            }

            public int getRepeatInterval() {
                return repeatInterval;
            }

            public void setRepeatInterval(final int repeatInterval) {
                this.repeatInterval = repeatInterval;
            }
        }
    }
}
