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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jasig.cas.authentication.principal.Principal;
import org.springframework.util.Assert;

/**
 * Immutable authentication event whose attributes may not change after creation.
 * This class is designed for serialization and is suitable for long-term storage.
 *
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @author Marvin S. Addison
 *
 * @since 3.0
 */
public final class ImmutableAuthentication implements Authentication, Serializable {

    /** UID for serializing. */
    private static final long serialVersionUID = 3206127526058061391L;

    /** Authentication date stamp. */
    private final long authenticatedDate;

    /** List of metadata about credentials presented at authentication. */
    private final List<CredentialMetaData> credentials;

    /** Authenticated principal. */
    private final Principal principal;

    /** Authentication metadata attributes. */
    private final Map<String, Object> attributes;

    /** Map of handler name to handler authentication success event. */
    private final Map<String, HandlerResult> successes;

    /** Map of handler name to handler authentication failure cause. */
    private final Map<String, Class<? extends Exception>> failures;

    /** No-arg constructor for serialization support. */
    private ImmutableAuthentication() {
        this.authenticatedDate = 0;
        this.credentials = null;
        this.principal = null;
        this.attributes = null;
        this.successes = null;
        this.failures = null;
    }

    /**
     * Creates a new instance with the given data.
     *
     * @param date Non-null authentication date.
     * @param credentials Non-null list of credential metadata containing at least one entry.
     * @param principal Non-null authenticated principal.
     * @param attributes Nullable map of authentication metadata.
     * @param successes Non-null map of authentication successes containing at least one entry.
     * @param failures Nullable map of authentication failures.
     */
    public ImmutableAuthentication(
            final Date date,
            final List<CredentialMetaData> credentials,
            final Principal principal,
            final Map<String, Object> attributes,
            final Map<String, HandlerResult> successes,
            final Map<String, Class<? extends Exception>> failures) {

        Assert.notNull(date, "Date cannot be null");
        Assert.notNull(credentials, "Credential cannot be null");
        Assert.notNull(principal, "Principal cannot be null");
        Assert.notNull(successes, "Successes cannot be null");
        Assert.notEmpty(credentials, "Credential cannot be empty");
        Assert.notEmpty(successes, "Successes cannot be empty");

        this.authenticatedDate = date.getTime();
        this.credentials = credentials;
        this.principal = principal;
        this.attributes = attributes.isEmpty() ? null : attributes;
        this.successes = successes;
        this.failures = failures.isEmpty() ? null : failures;
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
    }

    public Date getAuthenticatedDate() {
        return new ImmutableDate(this.authenticatedDate);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return wrap(this.attributes);
    }

    @Override
    public List<CredentialMetaData> getCredentials() {
        return Collections.unmodifiableList(this.credentials);
    }

    @Override
    public Map<String, HandlerResult> getSuccesses() {
        return Collections.unmodifiableMap(this.successes);
    }

    @Override
    public Map<String, Class<? extends Exception>> getFailures() {
        return wrap(this.failures);
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(97, 31);
        builder.append(this.principal);
        builder.append(this.authenticatedDate);
        builder.append(this.attributes);
        builder.append(this.credentials);
        builder.append(this.successes);
        builder.append(this.failures);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Authentication)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final Authentication other = (Authentication) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.principal, other.getPrincipal());
        builder.append(this.credentials, other.getCredentials());
        builder.append(this.successes, other.getSuccesses());
        builder.append(this.authenticatedDate, other.getAuthenticatedDate().getTime());
        builder.append(wrap(this.attributes), other.getAttributes());
        builder.append(wrap(this.failures), other.getFailures());
        return builder.isEquals();
    }

    /**
     * Wraps a possibly null map in an immutable wrapper.
     *
     * @param source Nullable map to wrap.
     *
     * @return {@link Collections#unmodifiableMap(java.util.Map)} if given map is not null, otherwise
     * {@link java.util.Collections#emptyMap()}.
     */
    private static <K, V> Map<K, V> wrap(final Map<K, V> source) {
        if (source != null) {
            return Collections.unmodifiableMap(source);
        }
        return Collections.emptyMap();
    }

    /**
     * Immutable date implementation that throws {@link UnsupportedOperationException} for setter methods.
     */
    private static final class ImmutableDate extends Date {

        /** No-arg constructor for serialization support. */
        private ImmutableDate() {}

        /**
         * Creates a new instance with the given epoch time in milliseconds.
         *
         * @param instant Milliseconds since the Unix epoch.
         */
        public ImmutableDate(final long instant) {
            super(instant);
        }

        @Override
        public void setYear(final int year) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDate(final int date) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHours(final int hours) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMinutes(final int minutes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSeconds(final int seconds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTime(final long time) {
            throw new UnsupportedOperationException();
        }
    }
}
