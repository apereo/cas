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
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.util.Assert;

/**
 * @author Scott Battaglia
 * @since 3.0.3
 */
public abstract class AbstractAuthentication implements Authentication {

    /** Serialization version marker. */
    private static final long serialVersionUID = -7375625871357827332L;

    /** A Principal object representing the authenticated entity. */
    private Principal principal;

    /** Associated authentication attributes. */
    private Map<String, Object> attributes;

    private Map<HandlerResult, Principal> successes;

    private Map<String, GeneralSecurityException> failures;


    protected void setAttributes(final Map<String, Object> attributes) {
        Assert.notNull(attributes, "Attributes cannot be null.");
        this.attributes = attributes;
    }


    public final Map<String, Object> getAttributes() {
        return this.attributes;
    }

    protected void setPrincipal(final Principal principal) {
        this.principal = principal;
    }

    public final Principal getPrincipal() {
        return this.principal;
    }

    protected void setSuccesses(Map<HandlerResult, Principal> successes) {
        Assert.notEmpty(successes, "Successes cannot be null or empty.");
        this.successes = successes;
    }

    public Map<HandlerResult, Principal> getSuccesses() {
        return this.successes;
    }

    protected void setFailures(final Map<String, GeneralSecurityException> failures) {
        Assert.notNull(failures , "Failures cannot be null.");
        this.failures = failures;
    }

    public Map<String, GeneralSecurityException> getFailures() {
        return this.failures;
    }

    public final boolean equals(final Object o) {
        if (o == null || !this.getClass().isAssignableFrom(o.getClass())) {
            return false;
        }

        Authentication a = (Authentication) o;

        return this.principal.equals(a.getPrincipal())
                && this.getAuthenticatedDate().equals(a.getAuthenticatedDate())
                && this.attributes.equals(a.getAttributes())
                && this.successes.equals(a.getSuccesses())
                && this.failures.equals(a.getFailures());
    }

    public final int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(49, 31);
        builder.append(this.principal);
        builder.append(this.getAuthenticatedDate());
        builder.append(this.successes);
        builder.append(this.failures);
        return builder.toHashCode();
    }

    public final String toString() {
        return "[Principal=" + this.principal.getId() + ", attributes="
            + this.attributes.toString() + "]";
    }
}
