package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.util.NamedObject;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.core.Ordered;

/**
 * The release policy that decides how attributes are to be released for a given service.
 * Each policy has the ability to apply an optional filter.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceAttributeReleasePolicy extends Serializable, Ordered, NamedObject {

    /**
     * Is authorized to release authentication attributes boolean.
     *
     * @return true/false
     */
    default boolean isAuthorizedToReleaseAuthenticationAttributes() {
        return true;
    }

    /**
     * Is authorized to release credential password?
     *
     * @return true /false
     */
    default boolean isAuthorizedToReleaseCredentialPassword() {
        return false;
    }

    /**
     * Is authorized to release proxy granting ticket?
     *
     * @return true /false
     */
    default boolean isAuthorizedToReleaseProxyGrantingTicket() {
        return false;
    }

    /**
     * Sets the attribute filter.
     *
     * @param filter the new attribute filter
     */
    default void setAttributeFilter(final RegisteredServiceAttributeFilter filter) {
    }

    /**
     * Gets consent policy.
     *
     * @return the consent policy
     */
    default RegisteredServiceConsentPolicy getConsentPolicy() {
        return null;
    }

    /**
     * Gets principal attribute repository that may control the fetching
     * and caching of attributes at release time from attribute repository sources..
     *
     * @return the principal attribute repository
     */
    RegisteredServicePrincipalAttributesRepository getPrincipalAttributesRepository();

    /**
     * Gets the attributes, having applied the filter.
     *
     * @param context the context
     * @return the attributes
     * @throws Throwable the throwable
     */
    Map<String, List<Object>> getAttributes(RegisteredServiceAttributeReleasePolicyContext context) throws Throwable;

    /**
     * Gets the attributes that qualify for consent.
     *
     * @param context the context
     * @return the attributes
     * @throws Throwable the throwable
     */
    default Map<String, List<Object>> getConsentableAttributes(final RegisteredServiceAttributeReleasePolicyContext context) throws Throwable {
        return getAttributes(context);
    }

    @Override
    default int getOrder() {
        return 0;
    }
    

    /**
     * Gets condition that controls whether this policy should be activated.
     *
     * @return the condition
     */
    default RegisteredServiceAttributeReleaseActivationCriteria getActivationCriteria() {
        return null;
    }
}
