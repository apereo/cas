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
package org.jasig.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Return a collection of allowed attributes for the principal, but additionally,
 * offers the ability to rename attributes on a per-service level.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class ReturnMappedAttributeReleasePolicy extends AbstractAttributeReleasePolicy {

    private static final long serialVersionUID = -6249488544306639050L;
    
    private Map<String, String> allowedAttributes;

    /**
     * Instantiates a new Return mapped attribute release policy.
     */
    public ReturnMappedAttributeReleasePolicy() {
        this(new TreeMap<String, String>());
    }

    /**
     * Instantiates a new Return mapped attribute release policy.
     *
     * @param allowedAttributes the allowed attributes
     */
    public ReturnMappedAttributeReleasePolicy(final Map<String, String> allowedAttributes) {
        this.allowedAttributes = allowedAttributes;
    }

    /**
     * Sets the allowed attributes.
     *
     * @param allowed the allowed attributes.
     */
    public void setAllowedAttributes(final Map<String, String> allowed) {
        this.allowedAttributes = allowed;
    }
    
    /**
     * Gets the allowed attributes.
     *
     * @return the allowed attributes
     */
    public Map<String, String> getAllowedAttributes() {
        return new TreeMap<String, String>(this.allowedAttributes);
    }
    
    @Override
    protected Map<String, Object> getAttributesInternal(final Map<String, Object> resolvedAttributes) {
        final Map<String, Object> attributesToRelease = new HashMap<>(resolvedAttributes.size());

        for (final Map.Entry<String, String> entry : this.allowedAttributes.entrySet()) {
            final String key = entry.getKey();
            final Object value = resolvedAttributes.get(key);

            if (value != null) {
                final String mappedAttributeName = entry.getValue();
                logger.debug("Found attribute [{}] in the list of allowed attributes, mapped to the name [{}]",
                        key, mappedAttributeName);
                attributesToRelease.put(mappedAttributeName, value);
            }
        }
        return attributesToRelease;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final ReturnMappedAttributeReleasePolicy rhs = (ReturnMappedAttributeReleasePolicy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.allowedAttributes, rhs.allowedAttributes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(allowedAttributes)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("allowedAttributes", allowedAttributes)
                .toString();
    }
}
