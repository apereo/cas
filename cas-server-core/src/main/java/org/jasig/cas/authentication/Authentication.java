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
package org.jasig.cas.authentication;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;

/**
 * <p>
 * The Authentication object represents a successful authentication request. It
 * contains the principal that the authentication request was made for as well
 * as the additional meta information such as the authenticated date and a map
 * of attributes.
 * </p>
 * <p>
 * An Authentication object must be serializable to permit persistance and
 * clustering.
 * </p>
 * <p>
 * Implementing classes must take care to ensure that the Map returned by
 * getAttributes is serializable by using a Serializable map such as HashMap.
 * </p>
 *
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public interface Authentication extends Serializable {

    /**
     * Method to obtain the Principal.
     *
     * @return a Principal implementation
     */
    Principal getPrincipal();

    /**
     * Method to retrieve the timestamp of when this Authentication object was
     * created.
     *
     * @return the date/time the authentication occurred.
     */
    Date getAuthenticationDate();

    /**
     * Attributes of the authentication (not the Principal).
     *
     * @return the map of attributes.
     */
    Map<String, Object> getAttributes();

    /**
     * Gets a list of metadata about the credentials supplied at authentication time.
     *
     * @return Non-null list of supplied credentials represented as metadata that should be considered safe for
     * long-term storage (e.g. serializable and secure with respect to credential disclosure). The order of items in
     * the returned list SHOULD be the same as the order in which the source credentials were presented and subsequently
     * processed.
     */
    List<CredentialMetaData> getCredentials();

    /**
     * Gets a map describing successful authentications produced by {@link AuthenticationHandler} components.
     *
     * @return Map of handler names to successful authentication result produced by that handler.
     */
    Map<String, HandlerResult> getSuccesses();

    /**
     * Gets a map describing failed authentications. By definition the failures here were not sufficient to prevent
     * authentication.
     *
     * @return Map of authentication handler names to the authentication errors produced on attempted authentication.
     */
    Map<String, Class<? extends Exception>> getFailures();
}
