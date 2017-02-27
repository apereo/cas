package org.apereo.cas.ticket;

/**
 * This is {@link TicketMetadataCatalogRegistrationPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface TicketMetadataCatalogRegistrationPlan {

    /**
     * Register ticket metadata.
     *
     * @param metadata the metadata
     */
    void registerTicketMetadata(TicketMetadata metadata);
}
