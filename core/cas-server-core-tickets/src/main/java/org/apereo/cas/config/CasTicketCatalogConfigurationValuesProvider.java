package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

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
     * ST storage name.
     *
     * @return st storage name function
     */

    default Function<CasConfigurationProperties, Long> getServiceTicketStorageTimeout() {
        return p -> p.getTicket().getSt().getTimeToKillInSeconds();
    }

    default Function<CasConfigurationProperties, String> getServiceTicketStorageName() {
        return p -> "serviceTicketsCache";
    }

    default Function<CasConfigurationProperties, Integer> getProxyTicketStorageTimeout() {
        return p -> p.getTicket().getPt().getTimeToKillInSeconds();
    }

    default Function<CasConfigurationProperties, String> getProxyTicketStorageName() {
        return p -> "proxyTicketsCache";
    }

    default Function<CasConfigurationProperties, Integer> getTicketGrantingTicketStorageTimeout() {
        return p -> p.getTicket().getTgt().getMaxTimeToLiveInSeconds();
    }

    default Function<CasConfigurationProperties, String> getTicketGrantingTicketStorageName() {
        return p -> "ticketGrantingTicketsCache";
    }

    default Function<CasConfigurationProperties, Integer> getProxyGrantingTicketStorageTimeout() {
        return p -> p.getTicket().getTgt().getMaxTimeToLiveInSeconds();
    }

    default Function<CasConfigurationProperties, String> getProxyGrantingTicketStorageName() {
        return p -> "proxyGrantingTicketsCache";
    }

    default Function<CasConfigurationProperties, Long> getTransientSessionStorageTimeout() {
        return p -> p.getTicket().getTst().getTimeToKillInSeconds();
    }

    default Function<CasConfigurationProperties, String> getTransientSessionStorageName() {
        return p -> "transientSessionTicketsCache";
    }
}
