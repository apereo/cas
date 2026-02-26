package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.NamedObject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

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
    Collection<? extends MetadataResolver> resolve(@Nullable SamlRegisteredService service,
                                                   CriteriaSet criteriaSet) throws Exception;

    /**
     * Resolve list.
     *
     * @param service the service
     * @return the collection
     * @throws Exception the exception
     */
    default Collection<? extends MetadataResolver> resolve(@Nullable final SamlRegisteredService service) throws Exception {
        return resolve(service, new CriteriaSet());
    }

    /**
     * Supports this service?
     *
     * @param service the service
     * @return true/false
     */
    boolean supports(@Nullable SamlRegisteredService service);

    /**
     * Is the resolver available and able to resolve metadata?
     * This method may contact the metadata source checking for
     * the source availability.
     *
     * @param service the service
     * @return true /false
     */
    default boolean isAvailable(@Nullable final SamlRegisteredService service) {
        return supports(service);
    }

    /**
     * Gets metadata manager.
     *
     * @return the metadata manager
     */
    @CanIgnoreReturnValue
    default Optional<SamlRegisteredServiceMetadataManager> getMetadataManager() {
        if (this instanceof final SamlRegisteredServiceMetadataManager mgr) {
            return Optional.of(mgr);
        }
        return Optional.empty();
    }

    /**
     * Gets source.
     *
     * @return the source
     */
    String getSourceId();
}
