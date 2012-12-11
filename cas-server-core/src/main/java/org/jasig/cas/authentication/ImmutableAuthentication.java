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

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.Instant;

/**
 * Default implementation of Authentication interface. ImmutableAuthentication
 * is an immutable object and thus its attributes cannot be changed.
 * <p>
 * Instanciators of the ImmutableAuthentication class must take care that the
 * map they provide is serializable (i.e. HashMap).
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0
 */
public final class ImmutableAuthentication extends AbstractAuthentication {

    /** UID for serializing. */
    private static final long serialVersionUID = -2566320547789416017L;

    /** Authentication timestamp. */
    private final Instant authenticatedDate;


    /**
     * Constructor that accepts both a principal and a map.
     * 
     * @param principal Principal representing user
     * @param attributes Authentication attributes map.
     * @throws IllegalArgumentException if the principal is null.
     */
    public ImmutableAuthentication(
            final Principal principal,
            final Map<String, Object> attributes,
            final Map<HandlerResult, Principal> successes,
            final Map<String, GeneralSecurityException> failures) {
        this.authenticatedDate = new Instant();
        setPrincipal(principal);
        setSuccesses(successes);
        setAttributes(attributes);
        setFailures(failures);
    }

    /**
     * Creates a new immutable clone of the given authentication.
     *
     * @param source Source to clone.
     */
    public ImmutableAuthentication(final Authentication source) {
        this.authenticatedDate = new Instant(source.getAuthenticatedDate().getTime());
        setPrincipal(source.getPrincipal());
        setAttributes(clone(source.getAttributes()));
        setSuccesses(clone(source.getSuccesses()));
        setFailures(clone(source.getFailures()));
    }

    public Date getAuthenticatedDate() {
        return this.authenticatedDate.toDate();
    }

    @Override
    protected void setSuccesses(final Map<HandlerResult, Principal> successes) {
        if (successes == null || successes.isEmpty()) {
            super.setSuccesses(Collections.<HandlerResult, Principal>emptyMap());
        } else {
            super.setSuccesses(Collections.unmodifiableMap(successes));
        }
    }

    @Override
    protected void setFailures(final Map<String, GeneralSecurityException> failures) {
        if (failures == null || failures.isEmpty()) {
            super.setFailures(Collections.<String, GeneralSecurityException>emptyMap());
        } else {
            super.setFailures(Collections.unmodifiableMap(failures));
        }
    }

    @Override
    protected void setAttributes(final Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            super.setAttributes(Collections.<String, Object>emptyMap());
        } else {
            super.setAttributes(Collections.unmodifiableMap(attributes));
        }
    }

    private <K, V> Map<K, V> clone(final Map<K, V> map) {
        if (map != null && !map.isEmpty()) {
            return new LinkedHashMap<K, V>(map);
        }
        return Collections.<K, V>emptyMap();
    }
}
