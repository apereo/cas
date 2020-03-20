package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredService extends Serializable, Comparable<RegisteredService> {

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
     * Get the authentication policy assigned to this service.
     * @return the policy
     */
    RegisteredServiceAuthenticationPolicy getAuthenticationPolicy();

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
     * Sets the identifier for this service. Use {@link #INITIAL_IDENTIFIER_VALUE} to indicate a branch new service definition.
     *
     * @param id the numeric identifier for the service.
     */
    void setId(long id);

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
    default String getDescription() {
        return StringUtils.EMPTY;
    }

    /**
     * Response determines how CAS should contact the matching service
     * typically with a ticket id. By default, the strategy is a 302 redirect.
     *
     * @return the response type
     * @see Response.ResponseType
     */
    String getResponseType();

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
     * Get the name of the attribute this service prefers to consume as username.
     *
     * @return an instance of {@link RegisteredServiceUsernameAttributeProvider}
     */
    RegisteredServiceUsernameAttributeProvider getUsernameAttributeProvider();

    /**
     * Get the acceptable usage policy linked to this application.
     *
     * @return an instance of {@link RegisteredServiceAcceptableUsagePolicy}
     */
    RegisteredServiceAcceptableUsagePolicy getAcceptableUsagePolicy();

    /**
     * Gets multifactor authentication policy.
     *
     * @return the authentication policy
     */
    RegisteredServiceMultifactorPolicy getMultifactorPolicy();

    /**
     * Gets proxy ticket expiration policy.
     *
     * @return the proxy ticket expiration policy
     */
    RegisteredServiceProxyTicketExpirationPolicy getProxyTicketExpirationPolicy();

    /**
     * Gets proxy granting ticket expiration policy.
     *
     * @return the proxy granting ticket expiration policy
     */
    RegisteredServiceProxyGrantingTicketExpirationPolicy getProxyGrantingTicketExpirationPolicy();

    /**
     * Gets service ticket expiration policy.
     *
     * @return the service ticket expiration policy
     */
    RegisteredServiceServiceTicketExpirationPolicy getServiceTicketExpirationPolicy();

    /**
     * Gets SSO participation strategy.
     *
     * @return the service ticket expiration policy
     */
    RegisteredServiceSingleSignOnParticipationPolicy getSingleSignOnParticipationPolicy();

    /**
     * Gets the set of handler names that must successfully authenticate credentials in order to access the service.
     * An empty set indicates that there are no requirements on particular authentication handlers; any will suffice.
     *
     * @return Non -null set of required handler names.
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
    Set<String> getRequiredHandlers();

    /**
     * Gets the set of  names that correspond to the environment to which this service belongs.
     * This may be used as a filter at runtime to narrow down the list of services
     * that are applicable to a particular environment, such as test, prod, etc.
     *
     * @return Non -null set of environment names.
     */
    Set<String> getEnvironments();

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
     * Returns the logout type of the service.
     *
     * @return the logout type of the service.
     */
    RegisteredServiceLogoutType getLogoutType();

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
     * Identifies the logout url that will be invoked
     * upon sending single-logout callback notifications.
     * This is an optional setting. When undefined, the service
     * url as is defined by {@link #getServiceId()} will be used
     * to handle logout invocations.
     *
     * @return the logout url for this service
     * @since 4.1
     */
    String getLogoutUrl();

    /**
     * Identifies the redirect url that will be used
     * when building a response to authentication requests.
     * The url is ultimately used to carry the service ticket
     * back to the application and will override the default
     * url which is tracked by the {@link WebApplicationService#getOriginalUrl()}.
     *
     * @return the redirect url for this service
     * @since 6.2
     */
    String getRedirectUrl();

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
    Map<String, RegisteredServiceProperty> getProperties();

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

    /**
     * Initialize the registered service instance by defaulting fields to specific
     * values or object instances, etc.
     */
    default void initialize() {
    }
}
