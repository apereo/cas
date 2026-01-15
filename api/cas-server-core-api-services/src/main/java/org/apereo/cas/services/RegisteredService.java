package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.core.Ordered;

/**
 * Interface for a service that can be registered by the Services Management
 * interface.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredService extends RegisteredServiceDefinition, Comparable<RegisteredService> {
    /**
     * Get the expiration policy rules for this service.
     *
     * @return the proxy policy
     */
    RegisteredServiceExpirationPolicy getExpirationPolicy();

    /**
     * Get the authentication policy assigned to this service.
     *
     * @return the policy
     */
    RegisteredServiceAuthenticationPolicy getAuthenticationPolicy();

    /**
     * Get service matching strategy used to evaluate
     * given service identifiers against this service.
     *
     * @return the strategy
     */
    RegisteredServiceMatchingStrategy getMatchingStrategy();

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
     * The unique identifier for this service.
     *
     * @return the unique identifier for this service.
     */
    String getServiceId();

    /**
     * Gets template name that acts
     * as the base version of this registered service.
     *
     * @return the template name
     */
    String getTemplateName();

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
     * @return the evaluation order
     */
    RegisteredService setEvaluationOrder(int evaluationOrder);

    /**
     * Get the name of the attribute this service prefers to consume as username.
     *
     * @return an instance of {@link RegisteredServiceUsernameAttributeProvider}
     */
    RegisteredServiceUsernameAttributeProvider getUsernameAttributeProvider();

    /**
     * Gets multifactor authentication policy.
     *
     * @return the authentication policy
     */
    RegisteredServiceMultifactorPolicy getMultifactorAuthenticationPolicy();

    /**
     * Gets ticket granting ticket expiration policy.
     *
     * @return the ticket granting ticket expiration policy
     */
    RegisteredServiceTicketGrantingTicketExpirationPolicy getTicketGrantingTicketExpirationPolicy();

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
     * Gets the attribute filtering policy to determine
     * how attributes are to be filtered and released for
     * this service.
     *
     * @return the attribute release policy
     */
    RegisteredServiceAttributeReleasePolicy getAttributeReleasePolicy();

    /**
     * Sets attribute release policy.
     *
     * @param policy the policy
     * @return the attribute release policy
     */
    RegisteredService setAttributeReleasePolicy(RegisteredServiceAttributeReleasePolicy policy);

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
     * Indicates the evaluation priority of this service definition.
     * Works in combination with {@code #getEvaluationOrder()}, allowing
     * registered services of the same category/type to be sorted and grouped
     * first before evaluation order for each category. In other words,
     * it acts as the first sort key for evaluating services.
     *
     * @return the evaluation priority
     */
    @JsonIgnore
    default int getEvaluationPriority() {
        return Ordered.LOWEST_PRECEDENCE;
    }

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

    /**
     * Assign id if undefined.
     *
     * @return the registered service
     */
    @CanIgnoreReturnValue
    default RegisteredService assignIdIfNecessary() {
        if (getId() == RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE) {
            setId(System.nanoTime());
        }
        return this;
    }


    /**
     * Determine whether the service is marked as internal.
     *
     * @return true/false
     */
    @JsonIgnore
    default boolean isInternal() {
        val property = RegisteredServiceProperty.RegisteredServiceProperties.INTERNAL_SERVICE_DEFINITION;
        return property.isAssignedTo(this, BooleanUtils::toBoolean)
            && property.getPropertyBooleanValue(this);
    }
}
