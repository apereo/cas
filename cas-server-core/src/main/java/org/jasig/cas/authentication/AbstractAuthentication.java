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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jasig.cas.util.SerialUtils;
import org.joda.time.Instant;
import org.springframework.util.Assert;

/**
 * Base class for mutable and immutable authentications.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.3
 * @see MutableAuthentication
 * @see ImmutableAuthentication
 */
public abstract class AbstractAuthentication implements Authentication, Serializable {

    /** Serialization support. */
    private static final long serialVersionUID = -809499206803382824L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];

    /** A Principal object representing the authenticated entity. */
    private Principal principal;

    /** Authentication timestamp. */
    private Instant authenticatedDate = new Instant();

    /** Associated authentication attributes. */
    private Map<String, Object> attributes;

    /** Metadata about successfully authenticated credentials. */
    private Map<HandlerResult, Principal> successes;

    /** Metadata about credentials that failed authentication. */
    private Map<String, GeneralSecurityException> failures;


    protected void setAttributes(final Map<String, Object> attributes) {
        Assert.notNull(attributes, "Attributes cannot be null.");
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    protected void setPrincipal(final Principal principal) {
        this.principal = principal;
    }

    @Override
    public final Principal getPrincipal() {
        return this.principal;
    }

    protected void setAuthenticatedDate(final Instant date) {
        this.authenticatedDate = date;
    }

    @Override
    public Date getAuthenticatedDate() {
        return this.authenticatedDate.toDate();
    }

    protected void setSuccesses(final Map<HandlerResult, Principal> successes) {
        Assert.notNull(successes, "Successes cannot be null.");
        this.successes = successes;
    }

    @Override
    public Map<HandlerResult, Principal> getSuccesses() {
        return this.successes;
    }

    protected void setFailures(final Map<String, GeneralSecurityException> failures) {
        Assert.notNull(failures , "Failures cannot be null.");
        this.failures = failures;
    }

    @Override
    public Map<String, GeneralSecurityException> getFailures() {
        return this.failures;
    }

    @Override
    public final boolean equals(final Object o) {
        if (!(o instanceof AbstractAuthentication)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        final Authentication a = (Authentication) o;
        return this.principal.equals(a.getPrincipal())
                && this.authenticatedDate.isEqual(a.getAuthenticatedDate().getTime())
                && this.attributes.equals(a.getAttributes())
                && this.successes.equals(a.getSuccesses())
                && this.failures.equals(a.getFailures());
    }

    @Override
    public final int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(49, 31);
        builder.append(this.principal);
        builder.append(this.authenticatedDate);
        builder.append(this.successes);
        builder.append(this.failures);
        return builder.toHashCode();
    }

    @Override
    public final String toString() {
        return this.principal.getId() + ":" + this.attributes;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        SerialUtils.writeObject(this.principal, out);
        SerialUtils.writeObject(this.authenticatedDate, out);
        SerialUtils.writeMap(this.attributes, out);
        SerialUtils.writeMap(this.successes, out);
        SerialUtils.writeMap(this.failures, out);
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.principal = SerialUtils.readObject(Principal.class, in);
        this.authenticatedDate = SerialUtils.readObject(Instant.class, in);
        this.attributes = SerialUtils.readMap(String.class, Object.class, in);
        this.successes = SerialUtils.readMap(HandlerResult.class, Principal.class, in);
        this.failures = SerialUtils.readMap(String.class, GeneralSecurityException.class, in);
    }
}
