package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for a service that can be registered by the Services Management
 * interface.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface RegisteredService extends Cloneable, Serializable, Comparable<RegisteredService> {

    /**
     * The logout type.
     */
    enum LogoutType {
        /**
         * For no SLO.
         */
        NONE,
        /**
         * For back channel SLO.
         */
        BACK_CHANNEL,
        /**
         * For front channel SLO.
         */
        FRONT_CHANNEL
    }

    /**
     * Initial ID value of newly created (but not persisted) registered service.
     */
    long INITIAL_IDENTIFIER_VALUE = -1;

    /**
     * Get the expiration policy rules for this service.
     *
     * @return the proxy policy
     */
    RegisteredServiceExpirationPolicy getExpirationPolicy();

    /**
     * Get the proxy policy rules for this service.
     *
     * @return the proxy policy
     */
    RegisteredServiceProxyPolicy getProxyPolicy();

    /**
     * The unique identifier for this service.
     *
     * @return the unique identifier for this service.
     */
    String getServiceId();

    /**
     * The numeric identifier for this service. Implementations
     * are expected to initialize the id with the value of {@link #INITIAL_IDENTIFIER_VALUE}.
     *
     * @return the numeric identifier for this service.
     */
    long getId();

    /**
     * Returns the name of the service.
     *
     * @return the name of the service.
     */
    String getName();

    /**
     * Returns a short theme name. Services do not need to have unique theme
     * names.
     *
     * @return the theme name associated with this service.
     */
    String getTheme();

    /**
     * Returns the description of the service.
     *
     * @return the description of the service.
     */
    String getDescription();

    /**
     * Gets the relative evaluation order of this service when determining
     * matches.
     *
     * @return Evaluation order relative to other registered services. Services with lower values will be evaluated for a match before others.
     */
    int getEvaluationOrder();

    /**
     * Sets the relative evaluation order of this service when determining
     * matches.
     *
     * @param evaluationOrder the service evaluation order
     */
    void setEvaluationOrder(int evaluationOrder);

    /**
     * Sets the identifer for this service. Use {@link #INITIAL_IDENTIFIER_VALUE} to indicate a branch new service definition.
     * @param id the numeric identifier for the service.
     */
    void setId(long id);

    /**
     * Get the name of the attribute this service prefers to consume as username.
     *
     * @return an instance of {@link RegisteredServiceUsernameAttributeProvider}
     */
    RegisteredServiceUsernameAttributeProvider getUsernameAttributeProvider();

    /**
     * Gets authentication policy.
     *
     * @return the authentication policy
     */
    RegisteredServiceMultifactorPolicy getMultifactorPolicy();

    /**
     * Gets the set of handler names that must successfully authenticate credentials in order to access the service.
     * An empty set indicates that there are no requirements on particular authentication handlers; any will suffice.
     *
     * @return Non -null set of required handler names.
     */
    Set<String> getRequiredHandlers();

    /**
     * Gets the access strategy that decides whether this registered
     * service is able to proceed with authentication requests.
     *
     * @return the access strategy
     */
    RegisteredServiceAccessStrategy getAccessStrategy();

    /**
     * Returns whether the service matches the registered service.
     * <p>Note, as of 3.1.2, matches are case insensitive.
     *
     * @param service the service to match.
     * @return true if they match, false otherwise.
     */
    boolean matches(Service service);

    /**
     * Returns whether the service id matches the registered service.
     *
     * @param serviceId the service id to match.
     * @return true if they match, false otherwise.
     */
    boolean matches(String serviceId);

    /**
     * Clone this service.
     *
     * @return the registered service
     */
    RegisteredService clone();

    /**
     * Returns the logout type of the service.
     *
     * @return the logout type of the service.
     */
    LogoutType getLogoutType();

    /**
     * Gets the attribute filtering policy to determine
     * how attributes are to be filtered and released for
     * this service.
     *
     * @return the attribute release policy
     */
    RegisteredServiceAttributeReleasePolicy getAttributeReleasePolicy();

    /**
     * Gets the logo image associated with this service.
     * The image mostly is served on the user interface
     * to identify this requesting service during authentication.
     *
     * @return URL of the image
     * @since 4.1
     */
    String getLogo();

    /**
     * Describes the canonical information url
     * where this service is advertised and may provide
     * help/guidance.
     *
     * @return the info url.
     */
    String getInformationUrl();

    /**
     * Links to the privacy policy of this service, if any.
     *
     * @return the link to privacy policy
     */
    String getPrivacyUrl();

    /**
     * Identifies the logout url that that will be invoked
     * upon sending single-logout callback notifications.
     * This is an optional setting. When undefined, the service
     * url as is defined by {@link #getServiceId()} will be used
     * to handle logout invocations.
     *
     * @return the logout url for this service
     * @since 4.1
     */
    URL getLogoutUrl();

    /**
     * Gets the public key associated with this service
     * that is used to authorize the request by
     * encrypting certain elements and attributes in
     * the CAS validation protocol response, such as
     * the PGT.
     *
     * @return the public key instance used to authorize the request
     * @since 4.1
     */
    RegisteredServicePublicKey getPublicKey();

    /**
     * Describes extra metadata about the service; custom fields
     * that could be used by submodules implementing additional
     * behavior on a per-service basis.
     *
     * @return map of custom metadata.
     * @since 4.2
     */
    default Map<String, RegisteredServiceProperty> getProperties() {
        return new LinkedHashMap<>(0);
    }

    /**
     * A list of contacts that are responsible for the clients that use
     * this service.
     *
     * @return list of Contacts
     * @since 5.2
     */
    List<RegisteredServiceContact> getContacts();

    /**
     * Gets friendly name of this service.
     * Typically describes the purpose of this service
     * and the return value is usually used for display purposes.
     *
     * @return the friendly name
     */
    @JsonIgnore
    default String getFriendlyName() {
        return this.getClass().getSimpleName();
    }
}
