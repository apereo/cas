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

import org.jasig.cas.authentication.principal.Principal;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Constructs immutable {@link Authentication} objects using the builder pattern.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface AuthenticationBuilder extends Serializable {

    /**
     * Gets the authenticated principal.
     *
     * @return Principal.
     */
    Principal getPrincipal();

    /**
     * Sets the principal returns this instance.
     *
     * @param p Authenticated principal.
     *
     * @return This builder instance.
     */
    AuthenticationBuilder setPrincipal(Principal p);

    /**
     * Adds metadata about a credential presented for authentication.
     *
     * @param credential Credential metadata.
     *
     * @return This builder instance.
     */
    AuthenticationBuilder addCredential(CredentialMetaData credential);

    /**
     * Adds an authentication metadata attribute key-value pair.
     *
     * @param key Authentication attribute key.
     * @param value Authentication attribute value.
     *
     * @return This builder instance.
     */
    AuthenticationBuilder addAttribute(String key, Object value);

    /**
     * Gets the authentication success map.
     *
     * @return Non-null map of handler names to successful handler authentication results.
     */
    Map<String, HandlerResult> getSuccesses();

    /**
     * Adds an authentication success to the map of handler names to successful authentication handler results.
     *
     * @param key Authentication handler name.
     * @param value Successful authentication handler result produced by handler of given name.
     *
     * @return This builder instance.
     */
    AuthenticationBuilder addSuccess(String key, HandlerResult value);

    /**
     * Sets the authentication date and returns this instance.
     *
     * @param d Authentication date.
     *
     * @return This builder instance.
     */
    AuthenticationBuilder setAuthenticationDate(Date d);

    /**
     * Creates an immutable authentication instance from builder data.
     *
     * @return Immutable authentication.
     */
    Authentication build();

    /**
     * Gets the authentication failure map.
     *
     * @return Non-null authentication failure map.
     */
    Map<String, Class<? extends Exception>> getFailures();

    /**
     * Adds an authentication failure to the map of handler names to the authentication handler failures.
     *
     * @param key Authentication handler name.
     * @param value Exception raised on handler failure to authenticate credential.
     *
     * @return This builder instance.
     */
    AuthenticationBuilder addFailure(String key, Class<? extends Exception> value);

    /**
     * Sets the authentication metadata attributes.
     *
     * @param attributes Non-null map of authentication metadata attributes.
     *
     * @return This builder instance.
     */
    AuthenticationBuilder setAttributes(Map<String, Object> attributes);

}
