package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TicketMetadata;
import org.apereo.cas.ticket.TicketMetadataRegistrationConfigurer;
import org.apereo.cas.ticket.TicketMetadataRegistrationPlan;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasProtocolCoreTicketMetadataRegistrationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casProtocolCoreTicketMetadataRegistrationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasProtocolCoreTicketMetadataRegistrationConfiguration implements TicketMetadataRegistrationConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasProtocolCoreTicketMetadataRegistrationConfiguration.class);

    @Override
    public final void configureTicketMetadataRegistrationPlan(final TicketMetadataRegistrationPlan plan) {
        LOGGER.debug("Registering core CAS protocol ticket metadata types...");

        buildAndRegisterProxyGrantingTicketMetadata(plan,
                buildTicketMetadata(plan, ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX, ProxyGrantingTicketImpl.class));

        buildAndRegisterProxyTicketMetadata(plan,
                buildTicketMetadata(plan, ProxyTicket.PROXY_TICKET_PREFIX, ProxyTicketImpl.class));

        buildAndRegisterServiceTicketMetadata(plan,
                buildTicketMetadata(plan, ServiceTicket.PREFIX, ServiceTicketImpl.class));

        buildAndRegisterTicketGrantingTicketMetadata(plan,
                buildTicketMetadata(plan, TicketGrantingTicket.PREFIX, TicketGrantingTicketImpl.class));
    }

    protected void buildAndRegisterProxyGrantingTicketMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        registerTicketMetadata(plan, metadata);
    }

    protected void buildAndRegisterProxyTicketMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        registerTicketMetadata(plan, metadata);
    }

    protected void buildAndRegisterServiceTicketMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        registerTicketMetadata(plan, metadata);
    }

    protected void buildAndRegisterTicketGrantingTicketMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        registerTicketMetadata(plan, metadata);
    }

    private TicketMetadata buildTicketMetadata(final TicketMetadataRegistrationPlan plan, final String prefix, final Class impl) {
        if (plan.containsTicketMetadata(prefix)) {
            return plan.findTicketMetadata(prefix);
        }
        return new TicketMetadata(impl, prefix);
    }

    /**
     * Register ticket metadata.
     *
     * @param plan     the plan
     * @param metadata the metadata
     */
    private void registerTicketMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        plan.registerTicketMetadata(metadata);
    }

}
