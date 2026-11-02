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
                                                   CriteriaSet criteriaSet)
            throws Exception;

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
     * Determines whether this resolver supports the service.
     *
     * @param service the service
     * @return true/false
     */
    boolean supports(@Nullable SamlRegisteredService service);

    /**
     * Determines whether the resolver is available and able to resolve metadata.
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
     * Gets the local files the metadata of the service is read from, if any, so that a change to them can be noticed.
     * A directory stands for the metadata files inside it.
     *
     * @param service the service
     * @return the files
     */
    default Collection<File> getMetadataSourceFiles(final SamlRegisteredService service) {
        return List.of();
    }

    /**
     * Describes where the metadata of the service comes from and the state of that source, such as a backup file
     * and how long ago its source last confirmed it, for operators.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     * @return the description
     */
    default Map<String, Object> describe(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        return Map.of();
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
}
