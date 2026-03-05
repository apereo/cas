package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.NamedObject;

/**
 * This is {@link SamlRegisteredServiceMetadataManager}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface SamlRegisteredServiceMetadataManager extends NamedObject {

    /**
     * Load list.
     *
     * @return the list
     */
    List<SamlMetadataDocument> load();
    
    /**
     * Save or update metadata document in the source.
     *
     * @param document the metadata document.
     */
    SamlMetadataDocument store(SamlMetadataDocument document);

    /**
     * Remove by id.
     *
     * @param id the id
     */
    void removeById(long id);

    /**
     * Remove by name.
     *
     * @param name the name
     */
    void removeByName(String name);

    /**
     * Find by name optional.
     *
     * @param name the name
     * @return the optional
     */
    Optional<SamlMetadataDocument> findByName(String name);

    /**
     * Find by id optional.
     *
     * @param id the id
     * @return the optional
     */
    Optional<SamlMetadataDocument> findById(long id);

    /**
     * Remove all.
     */
    void removeAll();

    /**
     * Gets source.
     *
     * @return the source
     */
    String getSourceId();
}
