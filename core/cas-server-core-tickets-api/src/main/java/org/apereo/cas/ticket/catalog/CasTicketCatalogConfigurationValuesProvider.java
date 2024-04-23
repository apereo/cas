package org.apereo.cas.ticket.catalog;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;

import org.springframework.context.ConfigurableApplicationContext;

import java.util.function.Function;

/**
 * Defines a set of methods to retrieve ticket catalog configuration values in a functional way for maximising the reuse
 * of configuration code.
 *
 * @author Dmitriy Kopylenko
 * @since 6.1.0
 */
public interface CasTicketCatalogConfigurationValuesProvider {

    /**
     * Storage/cache name for the service tickets.
     */
    String STORAGE_NAME_SERVICE_TICKETS = "serviceTicketsCache";
    /**
     * Storage/cache name for the proxy tickets.
     */
    String STORAGE_NAME_PROXY_TICKET = "proxyTicketsCache";
    /**
     * Storage/cache name for the ticket-granting tickets.
     */
    String STORAGE_NAME_TICKET_GRANTING_TICKETS = "ticketGrantingTicketsCache";
    /**
     * Storage/cache name for the proxy-granting tickets.
     */
    String STORAGE_NAME_PROXY_GRANTING_TICKETS = "proxyGrantingTicketsCache";
    /**
     * Storage/cache name for the transient session tickets.
     */
    String STORAGE_NAME_TRANSIENT_SESSION_TICKETS = "transientSessionTicketsCache";

    /**
     * ST storage timeout.
     *
     * @return ST storage timeout function
     */
    default Function<ConfigurableApplicationContext, Long> getServiceTicketStorageTimeout() {
        return ctx -> ctx.getBean(ExpirationPolicyBuilder.BEAN_NAME_SERVICE_TICKET_EXPIRATION_POLICY, ExpirationPolicyBuilder.class)
            .buildTicketExpirationPolicy().getTimeToLive();
    }

    /**
     * ST storage name.
     *
     * @return ST storage name function
     */
    default Function<CasConfigurationProperties, String> getServiceTicketStorageName() {
        return __ -> STORAGE_NAME_SERVICE_TICKETS;
    }

    /**
     * PT storage timeout.
     *
     * @return PT storage timeout function
     */
    default Function<ConfigurableApplicationContext, Long> getProxyTicketStorageTimeout() {
        return ctx -> ctx.getBean(ExpirationPolicyBuilder.BEAN_NAME_PROXY_TICKET_EXPIRATION_POLICY, ExpirationPolicyBuilder.class)
            .buildTicketExpirationPolicy().getTimeToLive();
    }

    /**
     * PT storage name.
     *
     * @return PT storage name function
     */
    default Function<CasConfigurationProperties, String> getProxyTicketStorageName() {
        return __ -> STORAGE_NAME_PROXY_TICKET;
    }

    /**
     * TGT storage timeout.
     *
     * @return TGT storage timeout function
     */
    default Function<ConfigurableApplicationContext, Long> getTicketGrantingTicketStorageTimeout() {
        return ctx -> ctx.getBean(ExpirationPolicyBuilder.BEAN_NAME_TICKET_GRANTING_TICKET_EXPIRATION_POLICY, ExpirationPolicyBuilder.class)
            .buildTicketExpirationPolicy().getTimeToLive();
    }

    /**
     * TGT storage name.
     *
     * @return TGT storage name function
     */
    default Function<CasConfigurationProperties, String> getTicketGrantingTicketStorageName() {
        return __ -> STORAGE_NAME_TICKET_GRANTING_TICKETS;
    }

    /**
     * PGT storage timeout.
     *
     * @return PGT storage timeout function
     */
    default Function<ConfigurableApplicationContext, Long> getProxyGrantingTicketStorageTimeout() {
        return ctx -> ctx.getBean(ExpirationPolicyBuilder.BEAN_NAME_PROXY_GRANTING_TICKET_EXPIRATION_POLICY, ExpirationPolicyBuilder.class)
            .buildTicketExpirationPolicy().getTimeToLive();
    }

    /**
     * PGT storage name.
     *
     * @return PGT storage name function
     */
    default Function<CasConfigurationProperties, String> getProxyGrantingTicketStorageName() {
        return __ -> STORAGE_NAME_PROXY_GRANTING_TICKETS;
    }

    /**
     * TST storage timeout.
     *
     * @return TST storage timeout function
     */
    default Function<ConfigurableApplicationContext, Long> getTransientSessionStorageTimeout() {
        return ctx -> ctx.getBean(ExpirationPolicyBuilder.BEAN_NAME_TRANSIENT_SESSION_TICKET_EXPIRATION_POLICY, ExpirationPolicyBuilder.class)
            .buildTicketExpirationPolicy().getTimeToLive();
    }

    /**
     * TST storage name.
     *
     * @return PGT storage name function
     */
    default Function<CasConfigurationProperties, String> getTransientSessionStorageName() {
        return p -> STORAGE_NAME_TRANSIENT_SESSION_TICKETS;
    }

    default Function<ConfigurableApplicationContext, Boolean> getProxyGrantingTicketCascadeRemovals() {
        return __ -> false;
    }

    default Function<ConfigurableApplicationContext, Boolean> getTicketGrantingTicketCascadeRemovals() {
        return __ -> false;
    }
}
