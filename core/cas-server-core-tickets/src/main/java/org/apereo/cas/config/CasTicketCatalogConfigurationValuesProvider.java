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
     * ST storage timeout.
     *
     * @return ST storage timeout function
     */
    default Function<CasConfigurationProperties, Long> getServiceTicketStorageTimeout() {
        return p -> p.getTicket().getSt().getTimeToKillInSeconds();
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
    default Function<CasConfigurationProperties, Integer> getProxyTicketStorageTimeout() {
        return p -> p.getTicket().getPt().getTimeToKillInSeconds();
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
    default Function<CasConfigurationProperties, Integer> getTicketGrantingTicketStorageTimeout() {
        return p -> p.getTicket().getTgt().getMaxTimeToLiveInSeconds();
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
    default Function<CasConfigurationProperties, Integer> getProxyGrantingTicketStorageTimeout() {
        return p -> p.getTicket().getTgt().getMaxTimeToLiveInSeconds();
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
    default Function<CasConfigurationProperties, Long> getTransientSessionStorageTimeout() {
        return p -> p.getTicket().getTst().getTimeToKillInSeconds();
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
