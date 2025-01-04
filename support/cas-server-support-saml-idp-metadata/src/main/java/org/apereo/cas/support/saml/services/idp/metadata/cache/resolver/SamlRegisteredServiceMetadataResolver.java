package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.NamedObject;

import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.NotImplementedException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

import java.util.Collection;

/**
 * This is {@link SamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface SamlRegisteredServiceMetadataResolver extends NamedObject {

    /**
     * Resolve list.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     * @return the list
     * @throws Exception the exception
     */
    Collection<? extends MetadataResolver> resolve(SamlRegisteredService service,
                                                   CriteriaSet criteriaSet) throws Exception;

    /**
     * Resolve list.
     *
     * @param service the service
     * @return the collection
     * @throws Exception the exception
     */
    default Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service) throws Exception {
        return resolve(service, new CriteriaSet());
    }

    /**
     * Supports this service?
     *
     * @param service the service
     * @return true/false
     */
    boolean supports(SamlRegisteredService service);

    /**
     * Save or update metadata document in the source.
     *
     * @param document the metadata document.
     */
    default void saveOrUpdate(final SamlMetadataDocument document) {
        throw new NotImplementedException("Operation saveOrUpdate is not implemented/supported");
    }


    /**
     * Is the resolver available and able to resolve metadata?
     * This method may contact the metadata source checking for
     * the source availability.
     *
     * @param service the service
     * @return true /false
     */
    default boolean isAvailable(final SamlRegisteredService service) {
        return supports(service);
    }
}
