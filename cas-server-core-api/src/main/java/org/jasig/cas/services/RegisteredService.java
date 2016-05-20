/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Service;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Interface for a service that can be registered by the Services Management
 * interface.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public interface RegisteredService extends Cloneable, Serializable {

    /** Initial ID value of newly created (but not persisted) registered service. **/
    long INITIAL_IDENTIFIER_VALUE = -Long.MAX_VALUE;

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
     * @return Evaluation order relative to other registered services.
     * Services with lower values will be evaluated for a match before others.
     */
    int getEvaluationOrder();

    /**
     * Sets the relative evaluation order of this service when determining
     * matches.
     * @param evaluationOrder the service evaluation order
     */
    void setEvaluationOrder(int evaluationOrder);

    /**
     * Get the name of the attribute this service prefers to consume as username.
     * @return an instance of {@link RegisteredServiceUsernameAttributeProvider}
     */
    RegisteredServiceUsernameAttributeProvider getUsernameAttributeProvider();

    /**
     * Gets the set of handler names that must successfully authenticate credentials in order to access the service.
     * An empty set indicates that there are no requirements on particular authentication handlers; any will suffice.
     *
     * @return Non-null set of required handler names.
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
     * Clone this service.
     *
     * @return the registered service
     * @throws CloneNotSupportedException the clone not supported exception
     */
    RegisteredService clone() throws CloneNotSupportedException;

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
    AttributeReleasePolicy getAttributeReleasePolicy();

    /**
     * Gets the logo image associated with this service.
     * The image mostly is served on the user interface
     * to identify this requesting service during authentication.
     * @return URL of the image
     * @since 4.1
     */
    URL getLogo();

    /**
     * Identifies the logout url that that will be invoked
     * upon sending single-logout callback notifications.
     * This is an optional setting. When undefined, the service
     * url as is defined by {@link #getServiceId()} will be used
     * to handle logout invocations.
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
     * @return the public key instance used to authorize the request
     * @since 4.1
     */
    RegisteredServicePublicKey getPublicKey();

    /**
     * Describes extra metadata about the service; custom fields
     * that could be used by submodules implementing additional
     * behavior on a per-service basis.
     * @return map of custom metadata.
     * @since 4.2
     */
    Map<String, RegisteredServiceProperty> getProperties();
    
}
