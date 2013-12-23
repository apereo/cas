/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;
import org.springframework.util.Assert;

/**
 * Constructs immutable {@link Authentication} objects using the builder pattern.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class AuthenticationBuilder {
    /** Authenticated principal. */
    private Principal principal;

    /** Credential metadata. */
    private List<CredentialMetaData> credentials = new ArrayList<CredentialMetaData>();

    /** Authentication metadata attributes. */
    private Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    /** Map of handler names to authentication successes. */
    private Map<String, HandlerResult> successes = new LinkedHashMap<String, HandlerResult>();

    /** Map of handler names to authentication failures. */
    private Map<String, Class<? extends Exception>> failures = new LinkedHashMap<String, Class<? extends Exception>>();


    /** Authentication date. */
    private Date authenticationDate;

    /**
     * Creates a new instance using the current date for the authentication date.
     */
    public AuthenticationBuilder() {
        authenticationDate = new Date();
    }

    /**
     * Creates a new instance using the current date for the authentication date and the given
     * principal for the authenticated principal.
     *
     * @param p Authenticated principal.
     */
    public AuthenticationBuilder(final Principal p) {
        this();
        this.principal = p;
    }

    /**
     * Gets the authentication date.
     *
     * @return Authentication date.
     */
    public Date getAuthenticationDate() {
        return authenticationDate;
    }

    /**
     * Sets the authentication date and returns this instance.
     *
     * @param d Authentication date.
     *
     * @return This builder instance.
     */
    public AuthenticationBuilder setAuthenticationDate(final Date d) {
        this.authenticationDate = d;
        return this;
    }

    /**
     * Gets the authenticated principal.
     *
     * @return Principal.
     */
    public Principal getPrincipal() {
        return this.principal;
    }

    /**
     * Sets the principal returns this instance.
     *
     * @param p Authenticated principal.
     *
     * @return This builder instance.
     */
    public AuthenticationBuilder setPrincipal(final Principal p) {
        this.principal = p;
        return this;
    }

    /**
     * Gets the list of credentials that were attempted to be authenticated.
     *
     * @return Non-null list of credentials that attempted authentication.
     */
    public List<CredentialMetaData> getCredentials() {
        return this.credentials;
    }

    /**
     * Sets the list of metadata about credentials presented for authentication.
     *
     * @param credentials Non-null list of credential metadata.
     *
     * @return This builder instance.
     */
    public AuthenticationBuilder setCredentials(final List<CredentialMetaData> credentials) {
        Assert.notNull(credentials, "Credential cannot be null");
        this.credentials.clear();
        for (final CredentialMetaData c : credentials) {
            this.credentials.add(c);
        }
        return this;
    }

    /**
     * Adds metadata about a credential presented for authentication.
     *
     * @param credential Credential metadata.
     *
     * @return This builder instance.
     */
    public AuthenticationBuilder addCredential(final CredentialMetaData credential) {
        this.credentials.add(credential);
        return this;
    }

    /**
     * Gets the authentication attribute map.
     *
     * @return Non-null authentication attribute map.
     */
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    /**
     * Sets the authentication metadata attributes.
     *
     * @param attributes Non-null map of authentication metadata attributes.
     *
     * @return This builder instance.
     */
    public AuthenticationBuilder setAttributes(final Map<String, Object> attributes) {
        Assert.notNull(attributes, "Attributes cannot be null");
        this.attributes.clear();
        for (final String name : attributes.keySet()) {
            this.attributes.put(name, attributes.get(name));
        }
        return this;
    }

    /**
     * Adds an authentication metadata attribute key-value pair.
     *
     * @param key Authentication attribute key.
     * @param value Authentication attribute value.
     *
     * @return This builder instance.
     */
    public AuthenticationBuilder addAttribute(final String key, final Object value) {
        this.attributes.put(key, value);
        return this;
    }

    /**
     * Gets the authentication success map.
     *
     * @return Non-null map of handler names to successful handler authentication results.
     */
    public Map<String, HandlerResult> getSuccesses() {
        return this.successes;
    }

    /**
     * Sets the authentication handler success map.
     *
     * @param successes Non-null map of handler names to successful handler authentication results.
     *
     * @return This builder instance.
     */
    public AuthenticationBuilder setSuccesses(final Map<String, HandlerResult> successes) {
        Assert.notNull(successes, "Successes cannot be null");
        this.successes.clear();
        for (final String handler : successes.keySet()) {
            this.successes.put(handler, successes.get(handler));
        }
        return this;
    }

    /**
     * Adds an authentication success to the map of handler names to successful authentication handler results.
     *
     * @param key Authentication handler name.
     * @param value Successful authentication handler result produced by handler of given name.
     *
     * @return This builder instance.
     */
    public AuthenticationBuilder addSuccess(final String key, final HandlerResult value) {
        this.successes.put(key, value);
        return this;
    }

    /**
     * Gets the authentication failure map.
     *
     * @return Non-null authentication failure map.
     */
    public Map<String, Class<? extends Exception>> getFailures() {
        return this.failures;
    }

    /**
     * Sets the authentication handler failure map.
     *
     * @param failures Non-null map of handler name to authentication failures.
     *
     * @return This builder instance.
     */
    public AuthenticationBuilder setFailures(final Map<String, Class<? extends Exception>> failures) {
        Assert.notNull(failures, "Failures cannot be null");
        this.failures.clear();
        for (final String handler : failures.keySet()) {
            this.failures.put(handler, failures.get(handler));
        }
        return this;
    }

    /**
     * Adds an authentication failure to the map of handler names to the authentication handler failures.
     *
     * @param key Authentication handler name.
     * @param value Exception raised on handler failure to authenticate credential.
     *
     * @return This builder instance.
     */
    public AuthenticationBuilder addFailure(final String key, final Class<? extends Exception> value) {
        this.failures.put(key, value);
        return this;
    }

    /**
     * Creates an immutable authentication instance from builder data.
     *
     * @return Immutable authentication.
     */
    public Authentication build() {
        return new ImmutableAuthentication(
                this.authenticationDate,
                this.credentials,
                this.principal,
                this.attributes,
                this.successes,
                this.failures);
    }

    /**
     * Creates a new builder initialized with data from the given authentication source.
     *
     * @param source Authentication source.
     *
     * @return New builder instance initialized with all fields in the given authentication source.
     */
    public static AuthenticationBuilder newInstance(final Authentication source) {
        final AuthenticationBuilder builder = new AuthenticationBuilder(source.getPrincipal());
        builder.setAuthenticationDate(source.getAuthenticatedDate());
        builder.setCredentials(source.getCredentials());
        builder.setSuccesses(source.getSuccesses());
        builder.setFailures(source.getFailures());
        builder.setAttributes(source.getAttributes());
        return builder;
    }
}
