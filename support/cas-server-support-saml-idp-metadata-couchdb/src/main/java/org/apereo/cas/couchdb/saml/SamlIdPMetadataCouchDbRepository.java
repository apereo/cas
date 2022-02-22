package org.apereo.cas.couchdb.saml;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

import java.util.Optional;

/**
 * This is {@link SamlIdPMetadataCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public interface SamlIdPMetadataCouchDbRepository {

    /**
     * Get all.
     *
     * @return the for all
     */
    CouchDbSamlIdPMetadataDocument getForAll();

    /**
     * Gets for service.
     *
     * @param registeredService the registered service
     * @return the for service
     */
    CouchDbSamlIdPMetadataDocument getForService(Optional<SamlRegisteredService> registeredService);
}
