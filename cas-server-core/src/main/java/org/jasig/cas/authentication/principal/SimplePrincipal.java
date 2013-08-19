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
package org.jasig.cas.authentication.principal;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.util.Assert;

/**
 * Simple implementation of a AttributePrincipal that exposes an unmodifiable
 * map of attributes.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public class SimplePrincipal implements Principal {
    /** Serialization support. */
    private static final long serialVersionUID = -1255260750151385796L;

    /** The unique identifier for the principal. */
    private final String id;

    /** Map of attributes for the Principal. */
    private Map<String, Object> attributes;

    /** No-arg constructor for serialization support. */
    private SimplePrincipal() {
        this.id = null;
    }

    public SimplePrincipal(final String id) {
        this(id, null);
    }

    public SimplePrincipal(final String id, final Map<String, Object> attributes) {
        Assert.notNull(id, "id cannot be null");
        this.id = id;
        this.attributes = attributes;
    }

    /**
     * @return An immutable map of principal attributes
     */
    public Map<String, Object> getAttributes() {
        return this.attributes == null
                ? Collections.<String, Object>emptyMap()
                : Collections.unmodifiableMap(this.attributes);
    }

    public String toString() {
        return this.id;
    }

    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(83, 31);
        builder.append(this.id);
        return builder.toHashCode();
    }

    public final String getId() {
        return this.id;
    }

    public boolean equals(final Object o) {
        return o instanceof SimplePrincipal && ((SimplePrincipal) o).getId().equals(this.id);
    }
}
