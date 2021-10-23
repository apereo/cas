package org.apereo.cas.config;

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
     * ST storage timeout.
     *
     * @return ST storage timeout function
     */
    default Function<ConfigurableApplicationContext, Long> getServiceTicketStorageTimeout() {
        return c -> c.getBean(ExpirationPolicyBuilder.BEAN_NAME_SERVICE_TICKET_EXPIRATION_POLICY, ExpirationPolicyBuilder.class)
            .buildTicketExpirationPolicy().getTimeToLive();
    }

    /**
     * ST storage name.
     *
     * @return ST storage name function
     */
    default Function<CasConfigurationProperties, String> getServiceTicketStorageName() {
        return p -> "serviceTicketsCache";
    }

    /**
     * PT storage timeout.
     *
     * @return PT storage timeout function
     */
    default Function<ConfigurableApplicationContext, Long> getProxyTicketStorageTimeout() {
        return c -> c.getBean(ExpirationPolicyBuilder.BEAN_NAME_PROXY_TICKET_EXPIRATION_POLICY, ExpirationPolicyBuilder.class)
            .buildTicketExpirationPolicy().getTimeToLive();
    }

    /**
     * PT storage name.
     *
     * @return PT storage name function
     */
    default Function<CasConfigurationProperties, String> getProxyTicketStorageName() {
        return p -> "proxyTicketsCache";
    }

    /**
     * TGT storage timeout.
     *
     * @return TGT storage timeout function
     */
    default Function<ConfigurableApplicationContext, Long> getTicketGrantingTicketStorageTimeout() {
        return c -> c.getBean(ExpirationPolicyBuilder.BEAN_NAME_TICKET_GRANTING_TICKET_EXPIRATION_POLICY, ExpirationPolicyBuilder.class)
            .buildTicketExpirationPolicy().getTimeToLive();
    }

    /**
     * TGT storage name.
     *
     * @return TGT storage name function
     */
    default Function<CasConfigurationProperties, String> getTicketGrantingTicketStorageName() {
        return p -> "ticketGrantingTicketsCache";
    }

    /**
     * PGT storage timeout.
     *
     * @return PGT storage timeout function
     */
    default Function<ConfigurableApplicationContext, Long> getProxyGrantingTicketStorageTimeout() {
        return c -> c.getBean(ExpirationPolicyBuilder.BEAN_NAME_PROXY_GRANTING_TICKET_EXPIRATION_POLICY, ExpirationPolicyBuilder.class)
            .buildTicketExpirationPolicy().getTimeToLive();
    }

    /**
     * PGT storage name.
     *
     * @return PGT storage name function
     */
    default Function<CasConfigurationProperties, String> getProxyGrantingTicketStorageName() {
        return p -> "proxyGrantingTicketsCache";
    }

    /**
     * TST storage timeout.
     *
     * @return TST storage timeout function
     */
    default Function<ConfigurableApplicationContext, Long> getTransientSessionStorageTimeout() {
        return c -> c.getBean(ExpirationPolicyBuilder.BEAN_NAME_TRANSIENT_SESSION_TICKET_EXPIRATION_POLICY, ExpirationPolicyBuilder.class)
            .buildTicketExpirationPolicy().getTimeToLive();
    }

    /**
     * TST storage name.
     *
     * @return PGT storage name function
     */
    default Function<CasConfigurationProperties, String> getTransientSessionStorageName() {
        return p -> "transientSessionTicketsCache";
    }
}
