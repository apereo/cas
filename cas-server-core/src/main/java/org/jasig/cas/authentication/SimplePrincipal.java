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
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jasig.cas.util.SerialUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Simple implementation of a AttributePrincipal that exposes an unmodifiable
 * map of attributes.
 * 
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision: 1.3 $ $Date: 2007/04/19 20:13:01 $
 * @since 3.1
 */
public class SimplePrincipal implements Principal, Serializable {

    /** Serialization support. */
    private static final long serialVersionUID = -5116401025146946946L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];

    /** The unique identifier for the principal. */
    private String id;

    /** Map of attributes for the Principal. */
    private Map<String, Object> attributes;

    public SimplePrincipal(final String id) {
        this(id, null);
    }

    public SimplePrincipal(final String id, final Map<String, Object> attributes) {
        Assert.notNull(id, "id cannot be null");
        this.id = id;
        this.attributes = CollectionUtils.isEmpty(attributes) ? Collections.<String, Object>emptyMap() : attributes;
    }

    @Override
    public final String getId() {
        return this.id;
    }

    /**
     * Gets an immutable view of attributes.
     *
     * @return Immutable attribute map.
     */
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(this.attributes);
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(101, 31);
        builder.append(this.id);
        builder.append(this.attributes);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }

        final SimplePrincipal p = (SimplePrincipal) o;
        return this.id.equals(p.getId()) && this.attributes.equals(p.getAttributes());
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        SerialUtils.writeObject(this.id, out);
        SerialUtils.writeMap(this.attributes, out);
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.id = SerialUtils.readObject(String.class, in);
        this.attributes = SerialUtils.readMap(String.class, Object.class, in);
    }
}
