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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of a {@link Principal} that exposes an unmodifiable
 * map of attributes. The attributes are cached upon construction and
 * will not be updated unless the principal is entirely and newly
 * resolved again.
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

    /** The Attribute repository.*/
    private final PrincipalAttributesRepository attributeRepository;

    /** No-arg constructor for serialization support. */
    private SimplePrincipal() {
        this.id = null;
        this.attributeRepository = new DefaultPrincipalAttributesRepository();
    }

    /**
     * Instantiates a new simple principal.
     *
     * @param id the id
     */
    private SimplePrincipal(final String id) {
        this(id, new HashMap<String, Object>());
    }

    /**
     * Instantiates a new simple principal.
     *
     * @param id the id
     * @param attributes the attributes
     */
    private SimplePrincipal(final String id, final Map<String, Object> attributes) {
        this(id, new DefaultPrincipalAttributesRepository(attributes));
    }

    /**
     * Instantiates a new Simple principal.
     *
     * @param id the id
     * @param attributeRepository the attribute repository
     */
    protected SimplePrincipal(final String id, final PrincipalAttributesRepository attributeRepository) {
        Assert.notNull(id, "id cannot be null");
        this.id = id;
        this.attributeRepository = attributeRepository;
    }

    /**
     * @return An immutable map of principal attributes
     */
    public Map<String, Object> getAttributes() {
        return this.attributeRepository.getAttributes(this.id);
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(83, 31);
        builder.append(this.id);
        return builder.toHashCode();
    }

    public final String getId() {
        return this.id;
    }
    @Override
    public boolean equals(final Object o) {
        return o instanceof SimplePrincipal && ((SimplePrincipal) o).getId().equals(this.id);
    }
}
