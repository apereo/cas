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

import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;
import org.springframework.util.Assert;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date: 2007-02-20 09:41:49 -0500 (Tue, 20 Feb
 * 2007) $
 * @since 3.0.3
 */
public abstract class AbstractAuthentication implements Authentication {

    /** A Principal object representing the authenticated entity. */
    private final Principal principal;

    /** Associated authentication attributes. */
    private final Map<String, Object> attributes;

    public AbstractAuthentication(final Principal principal,
        final Map<String, Object> attributes) {
        Assert.notNull(principal, "principal cannot be null");
        Assert.notNull(attributes, "attributes cannot be null");

        this.principal = principal;
        this.attributes = attributes;
    }

    public final Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public final Principal getPrincipal() {
        return this.principal;
    }

    public final boolean equals(final Object o) {
        if (o == null || !this.getClass().isAssignableFrom(o.getClass())) {
            return false;
        }

        Authentication a = (Authentication) o;

        return this.principal.equals(a.getPrincipal())
            && this.getAuthenticatedDate().equals(a.getAuthenticatedDate()) && this.attributes.equals(a.getAttributes());
    }

    public final int hashCode() {
        return 49 * this.principal.hashCode()
            ^ this.getAuthenticatedDate().hashCode();
    }

    public final String toString() {
        return "[Principal=" + this.principal.getId() + ", attributes="
            + this.attributes.toString() + "]";
    }
}
